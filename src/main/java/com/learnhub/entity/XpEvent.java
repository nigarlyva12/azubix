package com.learnhub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable audit record for every XP grant.
 *
 * reason values:
 *   ARTICLE_READ      – referenceId = articleId
 *   TOPIC_COMPLETE    – referenceId = topicId
 *   DAILY_LOGIN       – referenceId = null
 *   ACHIEVEMENT       – referenceId = achievementId
 */
@Entity
@Table(name = "xp_events",
       indexes = {
           @Index(columnList = "user_id, reason, reference_id"),
           @Index(columnList = "user_id, created_at")
       })
public class XpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, length = 30)
    private String reason;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public XpEvent() {}

    public XpEvent(User user, int amount, String reason, Long referenceId) {
        this.user = user;
        this.amount = amount;
        this.reason = reason;
        this.referenceId = referenceId;
    }

    // --- Getters ---

    public Long getId()               { return id; }
    public User getUser()             { return user; }
    public int getAmount()            { return amount; }
    public String getReason()         { return reason; }
    public Long getReferenceId()      { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
