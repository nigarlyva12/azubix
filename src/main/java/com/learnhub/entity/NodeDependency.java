package com.learnhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Directed edge between two RoadmapNodes.
 * fromNode → toNode means toNode depends on (comes after) fromNode.
 */
@Entity
@Table(name = "node_dependencies",
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_node_id", "to_node_id"}))
public class NodeDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_node_id", nullable = false)
    private RoadmapNode fromNode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_node_id", nullable = false)
    private RoadmapNode toNode;

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoadmapNode getFromNode() { return fromNode; }
    public void setFromNode(RoadmapNode fromNode) { this.fromNode = fromNode; }

    public RoadmapNode getToNode() { return toNode; }
    public void setToNode(RoadmapNode toNode) { this.toNode = toNode; }
}
