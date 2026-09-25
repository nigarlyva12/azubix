package com.learnhub.repository;

import com.learnhub.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Topic entity.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Find all topics that belong to a specific category
    List<Topic> findByCategoryId(Long categoryId);

    // Count topics in a category (useful for progress calculation)
    long countByCategoryId(Long categoryId);
}
