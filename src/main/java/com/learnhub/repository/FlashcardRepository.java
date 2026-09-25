package com.learnhub.repository;

import com.learnhub.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByTopicIdOrderByOrderIndexAsc(Long topicId);

    int countByTopicId(Long topicId);
}
