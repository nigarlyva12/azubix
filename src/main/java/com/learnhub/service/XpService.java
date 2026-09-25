package com.learnhub.service;

import com.learnhub.entity.*;
import com.learnhub.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Central service for the gamification system.
 *
 * Responsibilities:
 *  1. Seed achievement definitions once at startup
 *  2. Award XP for article reads, topic completions, daily logins
 *  3. Deduplicate awards so the same action can never earn XP twice
 *  4. Check and unlock achievements after every XP event
 *  5. Award bonus XP when a new achievement is unlocked
 *
 * XP values:
 *   Article read      +10 XP  (once per article)
 *   Topic complete    +50 XP  (once per topic)
 *   Daily login        +5 XP  (once per calendar day)
 *   Achievement bonus  varies
 */
@Service
@Transactional
public class XpService {

    // ── XP constants ────────────────────────────────────────────────────────
    public static final int XP_ARTICLE_READ   = 10;
    public static final int XP_TOPIC_COMPLETE = 50;
    public static final int XP_DAILY_LOGIN    =  5;

    // ── Reason tokens (stored in xp_events.reason) ─────────────────────────
    private static final String R_ARTICLE     = "ARTICLE_READ";
    private static final String R_TOPIC       = "TOPIC_COMPLETE";
    private static final String R_LOGIN       = "DAILY_LOGIN";
    private static final String R_ACHIEVEMENT = "ACHIEVEMENT";

    private final XpEventRepository          xpEventRepo;
    private final AchievementRepository      achievementRepo;
    private final UserAchievementRepository  userAchievementRepo;
    private final UserRepository             userRepo;
    private final ProgressService            progressService;

    public XpService(XpEventRepository xpEventRepo,
                     AchievementRepository achievementRepo,
                     UserAchievementRepository userAchievementRepo,
                     UserRepository userRepo,
                     ProgressService progressService) {
        this.xpEventRepo         = xpEventRepo;
        this.achievementRepo     = achievementRepo;
        this.userAchievementRepo = userAchievementRepo;
        this.userRepo            = userRepo;
        this.progressService     = progressService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Award XP for reading an article — deduplicated per article.
     * Also awards daily-login XP if this is the user's first action today.
     */
    public void awardArticleRead(User user, Long articleId) {
        awardDailyLoginIfNeeded(user);

        if (!xpEventRepo.existsByUserAndReasonAndReferenceId(user, R_ARTICLE, articleId)) {
            grantXp(user, XP_ARTICLE_READ, R_ARTICLE, articleId);
            checkAchievements(user, null, null);
        }
    }

    /**
     * Award XP for completing a topic — deduplicated per topic.
     * Also checks Category Master if the parent category is now 100 %.
     */
    public void awardTopicComplete(User user, Topic topic) {
        if (!xpEventRepo.existsByUserAndReasonAndReferenceId(user, R_TOPIC, topic.getId())) {
            grantXp(user, XP_TOPIC_COMPLETE, R_TOPIC, topic.getId());
            checkAchievements(user, topic, null);

            // Category Master — only if the category just hit 100 %
            Long catId = topic.getCategory().getId();
            if (progressService.calculateCategoryProgress(user.getId(), catId) == 100) {
                tryUnlock(user, "CATEGORY_MASTER");
            }
        }
    }

    /**
     * Award daily login XP — once per calendar day.
     * Safe to call on every page load; deduplication is built in.
     */
    public void awardDailyLoginIfNeeded(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);
        if (!xpEventRepo.existsByUserAndReasonAndCreatedAtBetween(
                user, R_LOGIN, startOfDay, endOfDay)) {
            grantXp(user, XP_DAILY_LOGIN, R_LOGIN, null);
            checkAchievements(user, null, null);
        }
    }

    /** Fetch all achievements a user has earned, newest first. */
    @Transactional(readOnly = true)
    public List<UserAchievement> getEarnedAchievements(User user) {
        return userAchievementRepo.findByUserOrderByEarnedAtDesc(user);
    }

    /** Fetch every defined achievement (for locked/unlocked display). */
    @Transactional(readOnly = true)
    public List<Achievement> getAllAchievements() {
        return achievementRepo.findAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    // INTERNALS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Write an XpEvent row and increment the user's cached totalXp.
     * Does NOT call checkAchievements — callers decide when to do that
     * to avoid recursive unlocking loops.
     */
    private void grantXp(User user, int amount, String reason, Long referenceId) {
        xpEventRepo.save(new XpEvent(user, amount, reason, referenceId));
        user.setTotalXp(user.getTotalXp() + amount);
        userRepo.save(user);
    }

    /**
     * Evaluate all milestone conditions and unlock any newly satisfied achievements.
     *
     * @param topic  the topic that was just completed (may be null)
     * @param extra  reserved for future context (unused, pass null)
     */
    private void checkAchievements(User user, Topic topic, Object extra) {
        long articlesRead    = xpEventRepo.countByUserAndReason(user, R_ARTICLE);
        long topicsCompleted = xpEventRepo.countByUserAndReason(user, R_TOPIC);
        int  streak          = user.getStreakCount() != null ? user.getStreakCount() : 0;
        int  totalXp         = user.getTotalXp();

        // Article milestones
        tryUnlockIf(user, "FIRST_ARTICLE", articlesRead >= 1);
        tryUnlockIf(user, "BOOKWORM",      articlesRead >= 5);
        tryUnlockIf(user, "SCHOLAR",       articlesRead >= 25);

        // Topic milestones
        tryUnlockIf(user, "FIRST_TOPIC",   topicsCompleted >= 1);

        // Streak milestones
        tryUnlockIf(user, "STREAK_3",      streak >= 3);
        tryUnlockIf(user, "STREAK_5",      streak >= 5);
        tryUnlockIf(user, "STREAK_7",      streak >= 7);

        // XP milestones (checked AFTER main XP is already added)
        tryUnlockIf(user, "CENTURY",       totalXp >= 100);
        tryUnlockIf(user, "HIGH_FLYER",    totalXp >= 500);
    }

    /** Unlock a specific achievement by key, regardless of condition. */
    private void tryUnlock(User user, String key) {
        achievementRepo.findByKey(key).ifPresent(a -> unlockIfNew(user, a));
    }

    /** Unlock an achievement only if the condition is true. */
    private void tryUnlockIf(User user, String key, boolean condition) {
        if (!condition) return;
        tryUnlock(user, key);
    }

    /**
     * Persist a new UserAchievement and grant the bonus XP.
     * Does NOT recurse into checkAchievements to prevent infinite loops.
     */
    private void unlockIfNew(User user, Achievement achievement) {
        if (userAchievementRepo.existsByUserAndAchievement(user, achievement)) return;

        userAchievementRepo.save(new UserAchievement(user, achievement));

        // Grant bonus XP directly — skip achievement re-checking
        if (achievement.getXpReward() > 0) {
            grantXp(user, achievement.getXpReward(), R_ACHIEVEMENT, achievement.getId());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACHIEVEMENT SEEDING
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates the built-in achievement definitions on first run.
     * Uses ApplicationReadyEvent so the full transaction infrastructure is live.
     * Safe to call multiple times — skips keys that already exist.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seedAchievements() {
        seed("FIRST_ARTICLE",   "First Article Read",  "Read your very first article",          "📖", 25);
        seed("BOOKWORM",        "Bookworm",            "Read 5 articles",                        "📚", 50);
        seed("SCHOLAR",         "Scholar",             "Read 25 articles",                       "🎓", 100);
        seed("FIRST_TOPIC",     "Topic Conquered",     "Complete your first topic",              "✅", 50);
        seed("STREAK_3",        "On a Roll",           "Maintain a 3-day learning streak",       "🔥", 75);
        seed("STREAK_5",        "5-Day Streak",        "Maintain a 5-day learning streak",       "⚡", 100);
        seed("STREAK_7",        "Week Warrior",        "Maintain a 7-day learning streak",       "🏆", 150);
        seed("CATEGORY_MASTER", "Category Master",     "Complete all topics in a category",      "🎯", 200);
        seed("CENTURY",         "Century Club",        "Accumulate 100 XP",                      "💯", 25);
        seed("HIGH_FLYER",      "High Flyer",          "Accumulate 500 XP",                      "🚀", 50);
    }

    private void seed(String key, String name, String description, String icon, int xpReward) {
        if (!achievementRepo.existsByKey(key)) {
            achievementRepo.save(new Achievement(key, name, description, icon, xpReward));
        }
    }
}
