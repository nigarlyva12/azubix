package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A single node (topic/step) inside a Roadmap.
 * Stores its visual position on the SVG canvas (posX, posY).
 */
@Entity
@Table(name = "roadmap_nodes")
public class RoadmapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    /** Estimated study time in minutes */
    private Integer estimatedMinutes = 60;

    /** SVG canvas position X */
    @Column(nullable = false)
    private double posX = 100;

    /** SVG canvas position Y */
    @Column(nullable = false)
    private double posY = 100;

    /** Phase/section name, e.g. "Foundations", "Advanced" */
    private String phase;

    /** Optional hex color override */
    private String colorOverride;

    /** If true, node is not accessible until dependencies are completed */
    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private Integer orderIndex = 0;

    /** Comma-separated tags */
    private String tags;

    @OneToMany(mappedBy = "fromNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeDependency> outgoing = new ArrayList<>();

    @OneToMany(mappedBy = "toNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeDependency> incoming = new ArrayList<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<NodeResource> resources = new ArrayList<>();

    // ── Enums ──────────────────────────────────────────────────────────────

    public enum Difficulty {
        EASY, MEDIUM, HARD, REVISION, MOCK_TEST;

        public String getColor() {
            return switch (this) {
                case EASY      -> "#2dd4bf";
                case MEDIUM    -> "#f59e0b";
                case HARD      -> "#f87171";
                case REVISION  -> "#60a5fa";
                case MOCK_TEST -> "#a78bfa";
            };
        }

        public String getLabel() {
            return switch (this) {
                case EASY      -> "Easy";
                case MEDIUM    -> "Medium";
                case HARD      -> "Hard";
                case REVISION  -> "Revision";
                case MOCK_TEST -> "Mock Test";
            };
        }
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Returns the effective display color: override > difficulty color */
    public String getEffectiveColor() {
        return (colorOverride != null && !colorOverride.isBlank())
                ? colorOverride
                : difficulty.getColor();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Roadmap getRoadmap() { return roadmap; }
    public void setRoadmap(Roadmap roadmap) { this.roadmap = roadmap; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public String getColorOverride() { return colorOverride; }
    public void setColorOverride(String colorOverride) { this.colorOverride = colorOverride; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public List<NodeDependency> getOutgoing() { return outgoing; }
    public void setOutgoing(List<NodeDependency> outgoing) { this.outgoing = outgoing; }

    public List<NodeDependency> getIncoming() { return incoming; }
    public void setIncoming(List<NodeDependency> incoming) { this.incoming = incoming; }

    public List<NodeResource> getResources() { return resources; }
    public void setResources(List<NodeResource> resources) { this.resources = resources; }
}
