package com.learnhub.repository;

import com.learnhub.entity.UserNodeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserNodeProgressRepository extends JpaRepository<UserNodeProgress, Long> {

    Optional<UserNodeProgress> findByUserIdAndNodeId(Long userId, Long nodeId);

    List<UserNodeProgress> findByUserIdAndNodeRoadmapId(Long userId, Long roadmapId);

    @Query("SELECT COUNT(p) FROM UserNodeProgress p WHERE p.user.id = :userId AND p.node.roadmap.id = :roadmapId AND p.completed = true")
    long countCompletedByUserAndRoadmap(@Param("userId") Long userId, @Param("roadmapId") Long roadmapId);

    @Query("SELECT COUNT(p) FROM UserNodeProgress p WHERE p.user.id = :userId AND p.node.roadmap.id = :roadmapId AND p.bookmarked = true")
    long countBookmarkedByUserAndRoadmap(@Param("userId") Long userId, @Param("roadmapId") Long roadmapId);
}
