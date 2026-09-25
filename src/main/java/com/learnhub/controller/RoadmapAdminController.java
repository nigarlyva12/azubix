package com.learnhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnhub.entity.*;
import com.learnhub.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;


@Controller
@RequestMapping("/admin/roadmaps")
public class RoadmapAdminController {

    private final RoadmapService roadmapService;
    private final ObjectMapper   objectMapper;

    public RoadmapAdminController(RoadmapService roadmapService, ObjectMapper objectMapper) {
        this.roadmapService = roadmapService;
        this.objectMapper   = objectMapper;
    }

   
    @GetMapping
    public String list(Model model) {
        List<Roadmap> roadmaps = roadmapService.findAll();
        model.addAttribute("roadmaps",   roadmaps);
        model.addAttribute("planTypes",  Roadmap.PlanType.values());
        model.addAttribute("examNames",  roadmapService.findAllExamNames());
        return "admin/roadmaps";
    }

   
    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam String examName,
                         @RequestParam Roadmap.PlanType planType,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) Integer estimatedHours,
                         RedirectAttributes ra) {
        Roadmap r = new Roadmap();
        r.setTitle(title);
        r.setExamName(examName);
        r.setPlanType(planType);
        r.setDescription(description);
        r.setEstimatedHours(estimatedHours);
        roadmapService.save(r);
        ra.addFlashAttribute("success", "Roadmap created! Now build the nodes.");
        return "redirect:/admin/roadmaps/" + r.getId() + "/builder";
    }

 
    @GetMapping("/{id}/builder")
    public String builder(@PathVariable Long id, Model model) {
        Roadmap roadmap = roadmapService.findById(id);
        Map<String, Object> graphData = roadmapService.buildGraphData(id);

        try {
            model.addAttribute("graphJson", objectMapper.writeValueAsString(graphData));
        } catch (Exception e) {
            model.addAttribute("graphJson", "{\"nodes\":[],\"edges\":[]}");
        }

        model.addAttribute("roadmap",      roadmap);
        model.addAttribute("difficulties", RoadmapNode.Difficulty.values());
        model.addAttribute("priorities",   RoadmapNode.Priority.values());
        model.addAttribute("resourceTypes",NodeResource.ResourceType.values());
        return "admin/roadmap-builder";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        roadmapService.publish(id);
        ra.addFlashAttribute("success", "Roadmap published!");
        return "redirect:/admin/roadmaps";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id, RedirectAttributes ra) {
        roadmapService.unpublish(id);
        ra.addFlashAttribute("success", "Roadmap unpublished.");
        return "redirect:/admin/roadmaps";
    }

    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        roadmapService.delete(id);
        ra.addFlashAttribute("success", "Roadmap deleted.");
        return "redirect:/admin/roadmaps";
    }

    
    @PostMapping("/{id}/nodes/create")
    @ResponseBody
    public ResponseEntity<?> createNode(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        Roadmap roadmap = roadmapService.findById(id);
        RoadmapNode node = new RoadmapNode();
        node.setRoadmap(roadmap);
        node.setTitle((String) body.getOrDefault("title", "New Node"));
        node.setDescription((String) body.get("description"));
        node.setPhase((String) body.get("phase"));
        node.setPosX(toDouble(body.get("posX"), 200));
        node.setPosY(toDouble(body.get("posY"), 200));
        node.setOrderIndex(roadmapService.getNodes(id).size());

        if (body.containsKey("difficulty"))
            node.setDifficulty(RoadmapNode.Difficulty.valueOf((String) body.get("difficulty")));
        if (body.containsKey("priority"))
            node.setPriority(RoadmapNode.Priority.valueOf((String) body.get("priority")));
        if (body.containsKey("estimatedMinutes"))
            node.setEstimatedMinutes(toInt(body.get("estimatedMinutes"), 60));

        RoadmapNode saved = roadmapService.saveNode(node);
        return ResponseEntity.ok(nodeToMap(saved));
    }

    @PostMapping("/{id}/nodes/{nodeId}/update")
    @ResponseBody
    public ResponseEntity<?> updateNode(@PathVariable Long id,
                                        @PathVariable Long nodeId,
                                        @RequestBody Map<String, Object> body) {
        RoadmapNode node = roadmapService.findNodeById(nodeId);
        if (body.containsKey("title"))       node.setTitle((String) body.get("title"));
        if (body.containsKey("description")) node.setDescription((String) body.get("description"));
        if (body.containsKey("phase"))       node.setPhase((String) body.get("phase"));
        if (body.containsKey("locked"))      node.setLocked((Boolean) body.get("locked"));
        if (body.containsKey("estimatedMinutes"))
            node.setEstimatedMinutes(toInt(body.get("estimatedMinutes"), 60));
        if (body.containsKey("difficulty"))
            node.setDifficulty(RoadmapNode.Difficulty.valueOf((String) body.get("difficulty")));
        if (body.containsKey("priority"))
            node.setPriority(RoadmapNode.Priority.valueOf((String) body.get("priority")));
        if (body.containsKey("colorOverride"))
            node.setColorOverride((String) body.get("colorOverride"));

        RoadmapNode saved = roadmapService.saveNode(node);
        return ResponseEntity.ok(nodeToMap(saved));
    }

    @PostMapping("/{id}/nodes/{nodeId}/position")
    @ResponseBody
    public ResponseEntity<?> updatePosition(@PathVariable Long id,
                                            @PathVariable Long nodeId,
                                            @RequestBody Map<String, Object> body) {
        double x = toDouble(body.get("x"), 0);
        double y = toDouble(body.get("y"), 0);
        roadmapService.updateNodePosition(nodeId, x, y);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/{id}/nodes/{nodeId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteNode(@PathVariable Long id,
                                        @PathVariable Long nodeId) {
        roadmapService.deleteNode(nodeId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

   
    @PostMapping("/{id}/edges/add")
    @ResponseBody
    public ResponseEntity<?> addEdge(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        Long from = toLong(body.get("from"));
        Long to   = toLong(body.get("to"));
        if (from == null || to == null || from.equals(to))
            return ResponseEntity.badRequest().body("Invalid nodes");
        roadmapService.addDependency(from, to);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/{id}/edges/remove")
    @ResponseBody
    public ResponseEntity<?> removeEdge(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        Long from = toLong(body.get("from"));
        Long to   = toLong(body.get("to"));
        roadmapService.removeDependency(from, to);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    
    @PostMapping("/{id}/nodes/{nodeId}/resources/add")
    @ResponseBody
    public ResponseEntity<?> addResource(@PathVariable Long id,
                                         @PathVariable Long nodeId,
                                         @RequestBody Map<String, Object> body) {
        RoadmapNode node = roadmapService.findNodeById(nodeId);
        NodeResource res = new NodeResource();
        res.setNode(node);
        res.setTitle((String) body.getOrDefault("title", "Resource"));
        res.setUrl((String) body.get("url"));
        res.setResourceType(NodeResource.ResourceType.valueOf(
                (String) body.getOrDefault("resourceType", "EXTERNAL")));
        res.setEstimatedMinutes(toInt(body.get("estimatedMinutes"), null));
        res.setOrderIndex(roadmapService.getResources(nodeId).size());
        NodeResource saved = roadmapService.saveResource(res);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "ok", true));
    }

    @PostMapping("/{id}/nodes/{nodeId}/resources/{resId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteResource(@PathVariable Long id,
                                            @PathVariable Long nodeId,
                                            @PathVariable Long resId) {
        roadmapService.deleteResource(resId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    
    private Map<String, Object> nodeToMap(RoadmapNode n) {
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
        return m;
    }

    private double toDouble(Object v, double def) {
        if (v == null) return def;
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return def; }
    }

    private Integer toInt(Object v, Integer def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
