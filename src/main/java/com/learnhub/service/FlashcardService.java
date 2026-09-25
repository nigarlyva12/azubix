package com.learnhub.service;

import com.learnhub.entity.Flashcard;
import com.learnhub.entity.User;
import com.learnhub.entity.UserFlashcardProgress;
import com.learnhub.repository.FlashcardRepository;
import com.learnhub.repository.UserFlashcardProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final UserFlashcardProgressRepository progressRepository;

    public FlashcardService(FlashcardRepository flashcardRepository,
                            UserFlashcardProgressRepository progressRepository) {
        this.flashcardRepository = flashcardRepository;
        this.progressRepository  = progressRepository;
    }

    // ── Flashcard management ──────────────────────────────────────────────

    public List<Flashcard> getFlashcardsForTopic(Long topicId) {
        return flashcardRepository.findByTopicIdOrderByOrderIndexAsc(topicId);
    }

    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id) {
        flashcardRepository.deleteById(id);
    }

    public Optional<Flashcard> findById(Long id) {
        return flashcardRepository.findById(id);
    }

    // ── Progress ──────────────────────────────────────────────────────────

    /**
     * Mark (or update) a flashcard as known/unknown for a user.
     */
    public void markKnown(User user, Flashcard flashcard, boolean known) {
        UserFlashcardProgress progress = progressRepository
                .findByUserIdAndFlashcardId(user.getId(), flashcard.getId())
                .orElse(new UserFlashcardProgress());

        progress.setUser(user);
        progress.setFlashcard(flashcard);
        progress.setKnown(known);
        progress.setLastReviewedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    /**
     * Returns a map of flashcardId -> known (boolean) for a given user + topic.
     */
    public Map<Long, Boolean> getProgressMap(Long userId, Long topicId) {
        List<UserFlashcardProgress> list =
                progressRepository.findByUserIdAndFlashcardTopicId(userId, topicId);
        Map<Long, Boolean> map = new HashMap<>();
        for (UserFlashcardProgress p : list) {
            map.put(p.getFlashcard().getId(), p.getKnown());
        }
        return map;
    }

    public int countKnown(Long userId, Long topicId) {
        return progressRepository.countByUserIdAndFlashcardTopicIdAndKnownTrue(userId, topicId);
    }
}
