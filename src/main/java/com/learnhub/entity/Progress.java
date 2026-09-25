package com.learnhub.entity;

import jakarta.persistence.*;

/**
 * Tracks whether a user has completed a specific topic.
 * This creates a many-to-many relationship between User and Topic
 * with an extra "completed" field.
 *
 * The unique constraint ensures one progress record per user-topic pair.
 */
@Entity
@Table(
    name = "progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"})
)
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user this progress belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Which topic this progress is about
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private boolean completed = false;

    // --- Constructors ---

    public Progress() {
    }

    public Progress(User user, Topic topic, boolean completed) {
        this.user = user;
        this.topic = topic;
        this.completed = completed;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
