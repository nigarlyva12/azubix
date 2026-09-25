package com.learnhub.service;

import com.learnhub.entity.Article;
import com.learnhub.entity.Comment;
import com.learnhub.entity.Role;
import com.learnhub.entity.User;
import com.learnhub.repository.ArticleRepository;
import com.learnhub.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    public CommentService(CommentRepository commentRepository,
                          ArticleRepository articleRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    /** Return all top-level comments for an article (replies are loaded via the entity). */
    public List<Comment> getCommentsForArticle(Long articleId) {
        return commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtAsc(articleId);
    }

    /** Count total comments (including replies) for an article. */
    public long countComments(Long articleId) {
        return commentRepository.countByArticleId(articleId);
    }

    /** Post a new top-level comment. */
    public Comment addComment(User user, Long articleId, String content) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found: " + articleId));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setArticle(article);
        comment.setContent(content.trim());
        return commentRepository.save(comment);
    }

    /** Post a reply to an existing comment. */
    public Comment addReply(User user, Long parentCommentId, String content) {
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + parentCommentId));

        Comment reply = new Comment();
        reply.setUser(user);
        reply.setArticle(parent.getArticle());
        reply.setParent(parent);
        reply.setContent(content.trim());
        return commentRepository.save(reply);
    }

    /**
     * Delete a comment. Only the author or an admin may delete.
     * Returns the article id so the controller can redirect properly.
     */
    public Long deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        boolean isOwner = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Not authorised to delete this comment");
        }

        Long articleId = comment.getArticle().getId();
        commentRepository.delete(comment);
        return articleId;
    }
}
