package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * A single exam question belonging to a Topic.
 * Supports three types: multiple-choice (4 options), true/false, fill-in-the-blank.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Question text / stem */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    /**
     * MULTIPLE_CHOICE: JSON array of 4 option strings, e.g. ["Berlin","Hamburg","München","Köln"]
     * TRUE_FALSE / FILL_BLANK: null / not used
     */
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    /**
     * MULTIPLE_CHOICE: "0", "1", "2", or "3"  (index into optionsJson)
     * TRUE_FALSE: "true" or "false"
     * FILL_BLANK: exact answer text (checked case-insensitively)
     */
    @Column(nullable = false)
    private String correctAnswer;

    /** Optional explanation shown after answering */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private Integer orderIndex = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // ── Question type enum ─────────────────────────────────────────────────

    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        FILL_BLANK
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
}
