package com.learnhub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * tracks user activity: article reads, topic completions, etc.
 */
@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // "READ", "COMPLETED", "STARTED"
    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String targetName;

    // Link to navigate to
    private String targetUrl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // --- Constructors ---
    public ActivityLog() {}

    public ActivityLog(User user, String action, String targetName, String targetUrl) {
        this.user = user;
        this.action = action;
        this.targetName = targetName;
        this.targetUrl = targetUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}