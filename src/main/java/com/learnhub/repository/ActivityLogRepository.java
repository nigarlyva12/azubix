package com.learnhub.repository;

import com.learnhub.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Get recent activity for a user, newest first
    List<ActivityLog> findTop10ByUserIdOrderByTimestampDesc(Long userId);

    // Count articles read by a user
    long countByUserIdAndAction(Long userId, String action);
}