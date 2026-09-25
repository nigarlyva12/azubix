package com.learnhub.repository;

import com.learnhub.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Fetch only top-level comments for an article (no replies),
     * oldest first so the thread reads naturally top-to-bottom.
     */
    List<Comment> findByArticleIdAndParentIsNullOrderByCreatedAtAsc(Long articleId);

    /** Count all comments (including replies) for an article. */
    long countByArticleId(Long articleId);
}
