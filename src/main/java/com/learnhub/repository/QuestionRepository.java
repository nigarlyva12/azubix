package com.learnhub.repository;

import com.learnhub.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopicIdOrderByOrderIndexAsc(Long topicId);

    int countByTopicId(Long topicId);
}
