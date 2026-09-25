package com.learnhub.controller;

import com.learnhub.entity.ActivityLog;
import com.learnhub.entity.Category;
import com.learnhub.entity.User;
import com.learnhub.entity.UserAchievement;
import com.learnhub.repository.UserRepository;
import com.learnhub.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class DashboardController {

    private final UserService     userService;
    private final CategoryService categoryService;
    private final ProgressService progressService;
    private final ActivityService activityService;
    private final UserRepository  userRepository;
    private final XpService       xpService;

    public DashboardController(UserService userService,
                               CategoryService categoryService,
                               ProgressService progressService,
                               ActivityService activityService,
                               UserRepository userRepository,
                               XpService xpService) {
        this.userService     = userService;
        this.categoryService = categoryService;
        this.progressService = progressService;
        this.activityService = activityService;
        this.userRepository  = userRepository;
        this.xpService       = xpService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        // --- Stats ---
        List<Category> categories = categoryService.findAll();
        long completedTopics = 0;
        int totalProgress = 0;
        int categoryCount = 0;
        Map<String, Integer> progressMap = new LinkedHashMap<>();

        for (Category category : categories) {
            int progress = progressService.calculateCategoryProgress(
                    user.getId(), category.getId());
            progressMap.put(category.getName(), progress);
            totalProgress += progress;
            categoryCount++;

            // Count completed topics
            List<Long> completedIds = progressService.getCompletedTopicIds(user.getId());
            for (var topic : category.getTopics()) {
                if (completedIds.contains(topic.getId())) {
                    completedTopics++;
                }
            }
        }

        int overallProgress = categoryCount > 0 ? totalProgress / categoryCount : 0;

        model.addAttribute("completedTopics", completedTopics);
        model.addAttribute("overallProgress", overallProgress);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("categories", categories);

        // --- articles read count ---
        long articlesRead = activityService.countArticlesRead(user.getId());
        model.addAttribute("articlesRead", articlesRead);

        // --- streak ---
        model.addAttribute("streak", user.getStreakCount());

        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        List<Boolean> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            if (user.getLastActiveDate() != null) {
                // Check if the day is within the streak range
                long daysAgo = ChronoUnit.DAYS.between(day, today);
                weekDays.add(daysAgo >= 0 && daysAgo < user.getStreakCount()
                        && !day.isAfter(today));
            } else {
                weekDays.add(false);
            }
        }
        model.addAttribute("weekDays", weekDays);

        List<ActivityLog> recentActivity = activityService.getRecentActivity(user.getId());
        model.addAttribute("recentActivity", recentActivity);

        model.addAttribute("totalXp", user.getTotalXp());
        List<UserAchievement> userAchievements = xpService.getEarnedAchievements(user);
        model.addAttribute("userAchievements", userAchievements);
        model.addAttribute("allAchievements", xpService.getAllAchievements());
        model.addAttribute("achievementCount", userAchievements.size());

        java.util.Set<Long> earnedIds = new java.util.HashSet<>();
        for (UserAchievement ua : userAchievements) {
            earnedIds.add(ua.getAchievement().getId());
        }
        model.addAttribute("earnedAchievementIds", earnedIds);
        
        List<Map<String, Object>> continueItems = new ArrayList<>();
        for (Category category : categories) {
            int progress = progressMap.get(category.getName());
            if (progress > 0 && progress < 100) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", category.getName());
                item.put("progress", progress);
                item.put("id", category.getId());
                continueItems.add(item);
            }
        }
        model.addAttribute("continueItems", continueItems);

        return "dashboard";
    }

    private User getUser(Authentication authentication) {
        if (authentication == null) return null;
        String email;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        } else {
            email = authentication.getName();
        }
        return userService.findByEmail(email).orElse(null);
    }
}