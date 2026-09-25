package com.learnhub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A learning roadmap for a specific exam and preparation intensity.
 * Each exam can have multiple roadmaps (one per PlanType).
 */
@Entity
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** e.g. "IHK Fachinformatiker", "AWS Cloud Practitioner" */
    @Column(nullable = false)
    private String examName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Column(nullable = false)
    private boolean published = false;

    @Column(nullable = false)
    private boolean draft = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Estimated total hours for this roadmap */
    private Integer estimatedHours;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoadmapNode> nodes = new ArrayList<>();

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum PlanType {
        SAFE_6M("Safe Plan", "6 Months", "Balanced pacing, deep understanding", "#2dd4bf"),
        FAST_3M("Fast Track", "3 Months", "Focused, higher weekly targets", "#a78bfa"),
        INTENSIVE_1M("Crazy Intensive", "1 Month", "Maximum effort, exam-only focus", "#f87171");

        public final String label;
        public final String duration;
        public final String tagline;
        public final String color;

        PlanType(String label, String duration, String tagline, String color) {
            this.label = label;
            this.duration = duration;
            this.tagline = tagline;
            this.color = color;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public boolean isDraft() { return draft; }
    public void setDraft(boolean draft) { this.draft = draft; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public List<RoadmapNode> getNodes() { return nodes; }
    public void setNodes(List<RoadmapNode> nodes) { this.nodes = nodes; }
}
