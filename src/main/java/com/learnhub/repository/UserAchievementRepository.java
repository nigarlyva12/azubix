package com.learnhub.repository;

import com.learnhub.entity.Achievement;
import com.learnhub.entity.User;
import com.learnhub.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserOrderByEarnedAtDesc(User user);

    boolean existsByUserAndAchievement(User user, Achievement achievement);

    long countByUser(User user);
}
