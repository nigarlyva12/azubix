package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * A learning resource attached to a RoadmapNode.
 * Supports videos, articles, PDFs, drive links, mock tests, etc.
 */
@Entity
@Table(name = "node_resources")
public class NodeResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private RoadmapNode node;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    /** Estimated minutes to consume this resource */
    private Integer estimatedMinutes;

    private Integer orderIndex = 0;

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum ResourceType {
        VIDEO, ARTICLE, PDF, NOTES, MOCK_TEST, DRIVE_LINK, GITHUB, EXTERNAL, MARKDOWN, PRACTICE_SHEET;

        public String getIcon() {
            return switch (this) {
                case VIDEO          -> "▶";
                case ARTICLE        -> "📄";
                case PDF            -> "📕";
                case NOTES          -> "📝";
                case MOCK_TEST      -> "📋";
                case DRIVE_LINK     -> "📁";
                case GITHUB         -> "⌥";
                case EXTERNAL       -> "🔗";
                case MARKDOWN       -> "✎";
                case PRACTICE_SHEET -> "📊";
            };
        }

        public String getLabel() {
            return switch (this) {
                case VIDEO          -> "Video";
                case ARTICLE        -> "Article";
                case PDF            -> "PDF";
                case NOTES          -> "Notes";
                case MOCK_TEST      -> "Mock Test";
                case DRIVE_LINK     -> "Drive";
                case GITHUB         -> "GitHub";
                case EXTERNAL       -> "Link";
                case MARKDOWN       -> "Note";
                case PRACTICE_SHEET -> "Practice";
            };
        }

        public String getColor() {
            return switch (this) {
                case VIDEO          -> "#f87171";
                case ARTICLE        -> "#60a5fa";
                case PDF            -> "#fb923c";
                case NOTES          -> "#a78bfa";
                case MOCK_TEST      -> "#2dd4bf";
                case DRIVE_LINK     -> "#4ade80";
                case GITHUB         -> "#e2e8f0";
                case EXTERNAL       -> "#94a3b8";
                case MARKDOWN       -> "#c4b5fd";
                case PRACTICE_SHEET -> "#fbbf24";
            };
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoadmapNode getNode() { return node; }
    public void setNode(RoadmapNode node) { this.node = node; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
