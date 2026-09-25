package com.learnhub.service;

import com.learnhub.dto.ArticleDto;
import com.learnhub.entity.Article;
import com.learnhub.entity.Topic;
import com.learnhub.repository.ArticleRepository;
import com.learnhub.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for article-related business logic.
 */
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TopicRepository topicRepository;

    public ArticleService(ArticleRepository articleRepository, TopicRepository topicRepository) {
        this.articleRepository = articleRepository;
        this.topicRepository = topicRepository;
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));
    }

    /**
     * Find all articles for a specific topic, ordered by newest first.
     */
    public List<Article> findByTopicId(Long topicId) {
        return articleRepository.findByTopicIdOrderByCreatedAtDesc(topicId);
    }

    /**
     * Create a new article from DTO.
     * Content is stored as HTML from the WYSIWYG editor.
     */
    public Article create(ArticleDto dto) {
        Topic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setTitleDe(blankToNull(dto.getTitleDe()));
        article.setContentDe(blankToNull(dto.getContentDe()));
        article.setTopic(topic);

        return articleRepository.save(article);
    }

    /**
     * Update an existing article.
     */
    public Article update(Long id, ArticleDto dto) {
        Article article = findById(id);

        Topic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setTitleDe(blankToNull(dto.getTitleDe()));
        article.setContentDe(blankToNull(dto.getContentDe()));
        article.setTopic(topic);

        return articleRepository.save(article);
    }

    public void delete(Long id) {
        articleRepository.deleteById(id);
    }

    /**
     * Returns the 7 most recently created articles for the home-page feed.
     */
    public List<Article> findRecent() {
        return articleRepository.findTop7ByOrderByCreatedAtDesc();
    }

    /**
     * Strips HTML tags from content and returns a plain-text teaser
     * truncated at the nearest word boundary before {@code maxLen} characters.
     */
    public String generateTeaser(String htmlContent, int maxLen) {
        if (htmlContent == null || htmlContent.isBlank()) return "";
        String text = htmlContent.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLen) return text;
        int breakAt = text.lastIndexOf(' ', maxLen);
        return text.substring(0, breakAt > 0 ? breakAt : maxLen) + "…";
    }

    /** Treats blank/whitespace strings as null so the DB column stays clean. */
    private static String blankToNull(String s) {
        return (s == null || s.isBlank() || s.equals("<p><br></p>")) ? null : s;
    }

    /**
     * Estimates reading time for an article's HTML content.
     * Strips HTML tags, counts words, and divides by 200 WPM (average reading speed).
     *
     * @param htmlContent the raw HTML content of the article
     * @return estimated reading time in minutes (minimum 1)
     */
    public int calculateReadingTime(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            return 1;
        }
        // Strip HTML tags and collapse whitespace
        String plainText = htmlContent.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        if (plainText.isEmpty()) {
            return 1;
        }
        int wordCount = plainText.split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }
}
