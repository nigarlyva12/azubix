package com.learnhub.repository;

import com.learnhub.entity.UserFlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long> {

    Optional<UserFlashcardProgress> findByUserIdAndFlashcardId(Long userId, Long flashcardId);

    List<UserFlashcardProgress> findByUserIdAndFlashcardTopicId(Long userId, Long topicId);

    int countByUserIdAndFlashcardTopicIdAndKnownTrue(Long userId, Long topicId);
}
