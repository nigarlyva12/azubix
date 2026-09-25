package com.learnhub.controller;

import com.learnhub.entity.Flashcard;
import com.learnhub.entity.Topic;
import com.learnhub.entity.User;
import com.learnhub.service.FlashcardService;
import com.learnhub.service.TopicService;
import com.learnhub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final TopicService     topicService;
    private final UserService      userService;

    public FlashcardController(FlashcardService flashcardService,
                               TopicService topicService,
                               UserService userService) {
        this.flashcardService = flashcardService;
        this.topicService     = topicService;
        this.userService      = userService;
    }

    @GetMapping("/topic/{topicId}/flashcards")
    public String showFlashcards(@PathVariable Long topicId, Model model, Authentication auth) {
        Topic topic = topicService.findById(topicId);

        List<Flashcard> cards = flashcardService.getFlashcardsForTopic(topicId);

        model.addAttribute("topic", topic);
        model.addAttribute("cards", cards);

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                Map<Long, Boolean> progressMap = flashcardService.getProgressMap(user.getId(), topicId);
                int knownCount = flashcardService.countKnown(user.getId(), topicId);
                model.addAttribute("progressMap", progressMap);
                model.addAttribute("knownCount",  knownCount);
            }
        }

        return "flashcards";
    }

    @PostMapping("/topic/{topicId}/flashcards/progress")
    @ResponseBody
    public ResponseEntity<?> updateProgress(@PathVariable Long topicId,
                                            @RequestBody Map<String, Object> body,
                                            Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long flashcardId = Long.parseLong(body.get("flashcardId").toString());
        boolean known    = Boolean.parseBoolean(body.get("known").toString());

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Flashcard card = flashcardService.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));

        flashcardService.markKnown(user, card, known);

        int knownCount = flashcardService.countKnown(user.getId(), topicId);
        int total      = flashcardService.getFlashcardsForTopic(topicId).size();
        return ResponseEntity.ok(Map.of("knownCount", knownCount, "total", total));
    }
}
