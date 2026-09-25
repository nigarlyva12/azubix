package com.learnhub.service;

import com.learnhub.entity.*;
import com.learnhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoadmapService {

    private final RoadmapRepository           roadmapRepo;
    private final RoadmapNodeRepository       nodeRepo;
    private final NodeDependencyRepository    depRepo;
    private final NodeResourceRepository      resourceRepo;
    private final UserNodeProgressRepository  progressRepo;

    public RoadmapService(RoadmapRepository roadmapRepo,
                          RoadmapNodeRepository nodeRepo,
                          NodeDependencyRepository depRepo,
                          NodeResourceRepository resourceRepo,
                          UserNodeProgressRepository progressRepo) {
        this.roadmapRepo  = roadmapRepo;
        this.nodeRepo     = nodeRepo;
        this.depRepo      = depRepo;
        this.resourceRepo = resourceRepo;
        this.progressRepo = progressRepo;
    }

    // ── Roadmap CRUD ───────────────────────────────────────────────────────

    public List<Roadmap> findAllPublished() {
        return roadmapRepo.findByPublishedTrueOrderByExamNameAscPlanTypeAsc();
    }

    public List<Roadmap> findAll() {
        return roadmapRepo.findAll();
    }

    public Roadmap findById(Long id) {
        return roadmapRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
    }

    public Roadmap save(Roadmap roadmap) {
        roadmap.setUpdatedAt(LocalDateTime.now());
        return roadmapRepo.save(roadmap);
    }

    public void delete(Long id) {
        roadmapRepo.deleteById(id);
    }

    public void publish(Long id) {
        Roadmap r = findById(id);
        r.setPublished(true);
        r.setDraft(false);
        roadmapRepo.save(r);
    }

    public void unpublish(Long id) {
        Roadmap r = findById(id);
        r.setPublished(false);
        r.setDraft(true);
        roadmapRepo.save(r);
    }

    public List<String> findAllExamNames() {
        return roadmapRepo.findDistinctExamNames();
    }

    public List<String> findPublishedExamNames() {
        return roadmapRepo.findDistinctPublishedExamNames();
    }

    // ── Node CRUD ──────────────────────────────────────────────────────────

    public List<RoadmapNode> getNodes(Long roadmapId) {
        return nodeRepo.findByRoadmapIdOrderByOrderIndexAsc(roadmapId);
    }

    public RoadmapNode findNodeById(Long id) {
        return nodeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found: " + id));
    }

    public RoadmapNode saveNode(RoadmapNode node) {
        return nodeRepo.save(node);
    }

    public void deleteNode(Long nodeId) {
        // Remove all dependencies involving this node first
        depRepo.deleteByFromNodeIdOrToNodeId(nodeId, nodeId);
        nodeRepo.deleteById(nodeId);
    }

    /** Update only the canvas position of a node (called from AJAX). */
    public void updateNodePosition(Long nodeId, double x, double y) {
        RoadmapNode node = findNodeById(nodeId);
        node.setPosX(x);
        node.setPosY(y);
        nodeRepo.save(node);
    }

    // ── Dependency management ──────────────────────────────────────────────

    public List<NodeDependency> getDependencies(Long roadmapId) {
        return depRepo.findByRoadmapId(roadmapId);
    }

    public NodeDependency addDependency(Long fromNodeId, Long toNodeId) {
        // Prevent duplicate
        if (depRepo.findByFromNodeIdAndToNodeId(fromNodeId, toNodeId).isPresent()) {
            return depRepo.findByFromNodeIdAndToNodeId(fromNodeId, toNodeId).get();
        }
        NodeDependency dep = new NodeDependency();
        dep.setFromNode(findNodeById(fromNodeId));
        dep.setToNode(findNodeById(toNodeId));
        return depRepo.save(dep);
    }

    public void removeDependency(Long fromNodeId, Long toNodeId) {
        depRepo.deleteByFromNodeIdAndToNodeId(fromNodeId, toNodeId);
    }

    // ── Resource management ────────────────────────────────────────────────

    public List<NodeResource> getResources(Long nodeId) {
        return resourceRepo.findByNodeIdOrderByOrderIndexAsc(nodeId);
    }

    public NodeResource saveResource(NodeResource resource) {
        return resourceRepo.save(resource);
    }

    public void deleteResource(Long resourceId) {
        resourceRepo.deleteById(resourceId);
    }

    // ── Progress tracking ──────────────────────────────────────────────────

    public void toggleComplete(User user, Long nodeId) {
        Optional<UserNodeProgress> existing = progressRepo.findByUserIdAndNodeId(user.getId(), nodeId);
        if (existing.isPresent()) {
            UserNodeProgress p = existing.get();
            p.setCompleted(!p.isCompleted());
            p.setCompletedAt(p.isCompleted() ? LocalDateTime.now() : null);
            progressRepo.save(p);
        } else {
            UserNodeProgress p = new UserNodeProgress();
            p.setUser(user);
            p.setNode(findNodeById(nodeId));
            p.setCompleted(true);
            p.setCompletedAt(LocalDateTime.now());
            progressRepo.save(p);
        }
    }

    public void toggleBookmark(User user, Long nodeId) {
        Optional<UserNodeProgress> existing = progressRepo.findByUserIdAndNodeId(user.getId(), nodeId);
        if (existing.isPresent()) {
            UserNodeProgress p = existing.get();
            p.setBookmarked(!p.isBookmarked());
            progressRepo.save(p);
        } else {
            UserNodeProgress p = new UserNodeProgress();
            p.setUser(user);
            p.setNode(findNodeById(nodeId));
            p.setBookmarked(true);
            progressRepo.save(p);
        }
    }

    /**
     * Returns a map of nodeId → UserNodeProgress for all nodes in a roadmap,
     * for the given user. Nodes with no record are absent from the map.
     */
    public Map<Long, UserNodeProgress> getProgressMap(Long userId, Long roadmapId) {
        List<UserNodeProgress> list = progressRepo.findByUserIdAndNodeRoadmapId(userId, roadmapId);
        return list.stream()
                .collect(Collectors.toMap(p -> p.getNode().getId(), p -> p));
    }

    public long countCompleted(Long userId, Long roadmapId) {
        return progressRepo.countCompletedByUserAndRoadmap(userId, roadmapId);
    }

    /**
     * Builds a serializable graph payload for the SVG canvas JS.
     * Returns a map with "nodes" and "edges" lists, safe to serialize to JSON.
     */
    public Map<String, Object> buildGraphData(Long roadmapId) {
        List<RoadmapNode> nodes = getNodes(roadmapId);
        List<NodeDependency> deps = getDependencies(roadmapId);

        List<Map<String, Object>> nodeList = nodes.stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",               n.getId());
            m.put("title",            n.getTitle());
            m.put("description",      n.getDescription());
            m.put("difficulty",       n.getDifficulty().name());
            m.put("difficultyLabel",  n.getDifficulty().getLabel());
            m.put("color",            n.getEffectiveColor());
            m.put("priority",         n.getPriority().name());
            m.put("estimatedMinutes", n.getEstimatedMinutes());
            m.put("phase",            n.getPhase());
            m.put("locked",           n.isLocked());
            m.put("posX",             n.getPosX());
            m.put("posY",             n.getPosY());
            m.put("resourceCount",    n.getResources().size());
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> edgeList = deps.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("from", d.getFromNode().getId());
            m.put("to",   d.getToNode().getId());
            return m;
        }).collect(Collectors.toList());

        return Map.of("nodes", nodeList, "edges", edgeList);
    }
}
