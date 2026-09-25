package com.learnhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnhub.entity.*;
import com.learnhub.service.RoadmapService;
import com.learnhub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final UserService    userService;
    private final ObjectMapper   objectMapper;

    public RoadmapController(RoadmapService roadmapService,
                             UserService userService,
                             ObjectMapper objectMapper) {
        this.roadmapService = roadmapService;
        this.userService    = userService;
        this.objectMapper   = objectMapper;
    }


    @GetMapping("/roadmaps")
    public String browse(Model model) {
        List<Roadmap> all = roadmapService.findAllPublished();

        // Group by exam name
        Map<String, List<Roadmap>> byExam = new LinkedHashMap<>();
        for (Roadmap r : all) {
            byExam.computeIfAbsent(r.getExamName(), k -> new ArrayList<>()).add(r);
        }

        model.addAttribute("byExam", byExam);
        model.addAttribute("planTypes", Roadmap.PlanType.values());
        return "roadmaps";
    }

    
    @GetMapping("/roadmaps/{id}")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        Roadmap roadmap = roadmapService.findById(id);
        Map<String, Object> graphData = roadmapService.buildGraphData(id);

        model.addAttribute("roadmap", roadmap);

        try {
            model.addAttribute("graphJson", objectMapper.writeValueAsString(graphData));
        } catch (Exception e) {
            model.addAttribute("graphJson", "{\"nodes\":[],\"edges\":[]}");
        }

        if (auth != null && auth.isAuthenticated()) {
            userService.findByEmail(auth.getName()).ifPresent(user -> {
                Map<Long, UserNodeProgress> progressMap = roadmapService.getProgressMap(user.getId(), id);
                long completed = roadmapService.countCompleted(user.getId(), id);
                long total     = roadmapService.getNodes(id).size();

                model.addAttribute("progressMap", progressMap);
                model.addAttribute("completedCount", completed);
                model.addAttribute("totalNodes", total);
                model.addAttribute("progressPct",
                        total > 0 ? (int)(completed * 100 / total) : 0);

                Map<Long, Boolean> jsProgress = new HashMap<>();
                progressMap.forEach((nodeId, p) -> jsProgress.put(nodeId, p.isCompleted()));
                Map<Long, Boolean> jsBookmarks = new HashMap<>();
                progressMap.forEach((nodeId, p) -> jsBookmarks.put(nodeId, p.isBookmarked()));
                try {
                    model.addAttribute("progressJson",  objectMapper.writeValueAsString(jsProgress));
                    model.addAttribute("bookmarkJson",  objectMapper.writeValueAsString(jsBookmarks));
                } catch (Exception ignored) {}
            });
        }

        if (!model.containsAttribute("progressJson"))  model.addAttribute("progressJson", "{}");
        if (!model.containsAttribute("bookmarkJson"))   model.addAttribute("bookmarkJson", "{}");
        if (!model.containsAttribute("completedCount")) model.addAttribute("completedCount", 0);
        if (!model.containsAttribute("totalNodes"))     model.addAttribute("totalNodes", roadmap.getNodes().size());
        if (!model.containsAttribute("progressPct"))    model.addAttribute("progressPct", 0);

        return "roadmap-view";
    }

    @PostMapping("/roadmaps/{id}/node/{nodeId}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleComplete(@PathVariable Long id,
                                            @PathVariable Long nodeId,
                                            Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).build();

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        roadmapService.toggleComplete(user, nodeId);
        long completed = roadmapService.countCompleted(user.getId(), id);
        long total     = roadmapService.getNodes(id).size();
        int  pct       = total > 0 ? (int)(completed * 100 / total) : 0;

        return ResponseEntity.ok(Map.of(
                "completed", completed,
                "total",     total,
                "pct",       pct
        ));
    }

   
    @PostMapping("/roadmaps/{id}/node/{nodeId}/bookmark")
    @ResponseBody
    public ResponseEntity<?> toggleBookmark(@PathVariable Long id,
                                            @PathVariable Long nodeId,
                                            Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).build();

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        roadmapService.toggleBookmark(user, nodeId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/roadmaps/{id}/node/{nodeId}/resources")
    @ResponseBody
    public ResponseEntity<?> getNodeResources(@PathVariable Long id,
                                              @PathVariable Long nodeId) {
        RoadmapNode node = roadmapService.findNodeById(nodeId);
        List<NodeResource> resources = roadmapService.getResources(nodeId);

        List<Map<String, Object>> resList = new ArrayList<>();
        for (NodeResource r : resources) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",               r.getId());
            m.put("title",            r.getTitle());
            m.put("url",              r.getUrl());
            m.put("type",             r.getResourceType().name());
            m.put("typeLabel",        r.getResourceType().getLabel());
            m.put("typeColor",        r.getResourceType().getColor());
            m.put("typeIcon",         r.getResourceType().getIcon());
            m.put("estimatedMinutes", r.getEstimatedMinutes());
            resList.add(m);
        }

        return ResponseEntity.ok(Map.of(
                "id",          node.getId(),
                "title",       node.getTitle(),
                "description", node.getDescription() != null ? node.getDescription() : "",
                "difficulty",  node.getDifficulty().getLabel(),
                "color",       node.getEffectiveColor(),
                "phase",       node.getPhase() != null ? node.getPhase() : "",
                "estimatedMinutes", node.getEstimatedMinutes(),
                "resources",   resList
        ));
    }
}
