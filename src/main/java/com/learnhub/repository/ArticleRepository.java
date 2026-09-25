package com.learnhub.repository;

import com.learnhub.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Article entity.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Find all articles that belong to a specific topic
    List<Article> findByTopicId(Long topicId);

    // Find articles ordered by creation date (newest first)
    List<Article> findByTopicIdOrderByCreatedAtDesc(Long topicId);

    // Most-recent N articles for the home-page feed (Spring Data derived query)
    List<Article> findTop7ByOrderByCreatedAtDesc();
}
