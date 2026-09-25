package com.learnhub.service;

import com.learnhub.entity.ActivityLog;
import com.learnhub.entity.User;
import com.learnhub.repository.ActivityLogRepository;
import com.learnhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityLogRepository activityLogRepository,
                           UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Log an activity and update the user's streak.
     */
    public void logActivity(User user, String action, String targetName, String targetUrl) {
        ActivityLog log = new ActivityLog(user, action, targetName, targetUrl);
        activityLogRepository.save(log);

        // Update streak
        updateStreak(user);
    }

    /**
     * Get recent activity for a user.
     */
    public List<ActivityLog> getRecentActivity(Long userId) {
        return activityLogRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Count how many articles a user has read.
     */
    public long countArticlesRead(Long userId) {
        return activityLogRepository.countByUserIdAndAction(userId, "READ");
    }

    /**
     * Update the user's learning streak.
     * If they were active yesterday, increment. If today already counted, skip.
     * Otherwise reset to 1.
     */
    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveDate();

        if (lastActive == null) {
            // First ever activity
            user.setStreakCount(1);
        } else if (lastActive.equals(today)) {
            // Already active today, don't increment
            return;
        } else if (lastActive.equals(today.minusDays(1))) {
            // Active yesterday — streak continues
            user.setStreakCount(user.getStreakCount() + 1);
        } else {
            // Missed a day — reset streak
            user.setStreakCount(1);
        }

        user.setLastActiveDate(today);
        userRepository.save(user);
    }
}