package com.learnhub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an article (the actual learning content).
 * Content is stored as HTML (from the WYSIWYG editor).
 *
 * Each article belongs to one topic (Many-to-One).
 * A topic can have multiple articles (e.g., "Part 1", "Part 2").
 */
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Store HTML content from the rich text editor
    // Using @Lob + TEXT type so there's no character limit
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // ── German translation (optional — falls back to EN when blank) ──────────

    @Column(name = "title_de")
    private String titleDe;

    @Column(name = "content_de", columnDefinition = "TEXT")
    private String contentDe;

    // Many articles can belong to one topic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Constructors ---

    public Article() {
    }

    public Article(String title, String content, Topic topic) {
        this.title = title;
        this.content = content;
        this.topic = topic;
    }

    /**
     * Automatically set createdAt before first save.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Automatically update updatedAt before every update.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTitleDe() { return titleDe; }
    public void setTitleDe(String titleDe) { this.titleDe = titleDe; }

    public String getContentDe() { return contentDe; }
    public void setContentDe(String contentDe) { this.contentDe = contentDe; }

    // ── Locale-aware helpers (used by controllers) ──────────────────────────

    /** Returns the German title if set, otherwise falls back to English. */
    public String getLocalizedTitle(String lang) {
        if ("de".equalsIgnoreCase(lang) && titleDe != null && !titleDe.isBlank()) {
            return titleDe;
        }
        return title;
    }

    /** Returns the German content if set, otherwise falls back to English. */
    public String getLocalizedContent(String lang) {
        if ("de".equalsIgnoreCase(lang) && contentDe != null && !contentDe.isBlank()) {
            return contentDe;
        }
        return content;
    }
}
