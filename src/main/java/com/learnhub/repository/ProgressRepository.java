package com.learnhub.repository;

import com.learnhub.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Progress entity.
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Find a specific progress record for a user + topic combination
    Optional<Progress> findByUserIdAndTopicId(Long userId, Long topicId);

    // Find all progress records for a user
    List<Progress> findByUserId(Long userId);

    // Count how many topics a user has completed in a specific category
    // This uses a custom JPQL query that joins Progress -> Topic -> Category
    @Query("SELECT COUNT(p) FROM Progress p " +
           "WHERE p.user.id = :userId " +
           "AND p.topic.category.id = :categoryId " +
           "AND p.completed = true")
    long countCompletedByUserAndCategory(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId
    );

    // Find all completed topic IDs for a user (useful for checking completion status)
    @Query("SELECT p.topic.id FROM Progress p " +
           "WHERE p.user.id = :userId AND p.completed = true")
    List<Long> findCompletedTopicIdsByUserId(@Param("userId") Long userId);
}
