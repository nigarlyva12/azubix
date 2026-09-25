package com.learnhub.repository;

import com.learnhub.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByUserIdAndTopicIdOrderByCompletedAtDesc(Long userId, Long topicId);

    Optional<ExamAttempt> findTopByUserIdAndTopicIdOrderByScoreDesc(Long userId, Long topicId);

    List<ExamAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);

    int countByUserIdAndTopicId(Long userId, Long topicId);
}
