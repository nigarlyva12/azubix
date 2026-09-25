package com.learnhub.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnhub.entity.*;
import com.learnhub.repository.ExamAttemptRepository;
import com.learnhub.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamService {

    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ObjectMapper objectMapper;

    public ExamService(QuestionRepository questionRepository,
                       ExamAttemptRepository examAttemptRepository,
                       ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.objectMapper = objectMapper;
    }

    // ── Question management ───────────────────────────────────────────────

    public List<Question> getQuestionsForTopic(Long topicId) {
        return questionRepository.findByTopicIdOrderByOrderIndexAsc(topicId);
    }

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    public Optional<Question> findQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    /**
     * Parse the optionsJson of a MULTIPLE_CHOICE question into a List<String>.
     * Returns an empty list for other types or on parse error.
     */
    public List<String> parseOptions(Question question) {
        if (question.getType() != Question.QuestionType.MULTIPLE_CHOICE
                || question.getOptionsJson() == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(question.getOptionsJson(),
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ── Exam submission ───────────────────────────────────────────────────

    /**
     * Grades the submitted answers, persists the attempt, and returns it.
     *
     * @param answers Map of questionId (as String) -> userAnswer
     */
    public ExamAttempt submitExam(User user, Topic topic, Map<String, String> answers) {
        List<Question> questions = questionRepository.findByTopicIdOrderByOrderIndexAsc(topic.getId());

        int correct = 0;
        for (Question q : questions) {
            String userAnswer = answers.get(String.valueOf(q.getId()));
            if (userAnswer != null && isCorrect(q, userAnswer)) {
                correct++;
            }
        }

        int total = questions.size();
        boolean passed = total > 0 && (correct * 100 / total) >= 60;

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setTopic(topic);
        attempt.setScore(correct);
        attempt.setTotalQuestions(total);
        attempt.setPassed(passed);
        attempt.setCompletedAt(LocalDateTime.now());

        try {
            attempt.setAnswersJson(objectMapper.writeValueAsString(answers));
        } catch (Exception e) {
            attempt.setAnswersJson("{}");
        }

        return examAttemptRepository.save(attempt);
    }

    private boolean isCorrect(Question q, String userAnswer) {
        String correct = q.getCorrectAnswer().trim().toLowerCase();
        String answer  = userAnswer.trim().toLowerCase();

        if (q.getType() == Question.QuestionType.FILL_BLANK) {
            // Accept if the user's answer contains the correct answer or vice versa
            return correct.equals(answer)
                    || correct.contains(answer)
                    || answer.contains(correct);
        }
        return correct.equals(answer);
    }

    // ── Result helpers ────────────────────────────────────────────────────

    public Optional<ExamAttempt> findAttemptById(Long id) {
        return examAttemptRepository.findById(id);
    }

    public Optional<ExamAttempt> getBestAttempt(Long userId, Long topicId) {
        return examAttemptRepository.findTopByUserIdAndTopicIdOrderByScoreDesc(userId, topicId);
    }

    public List<ExamAttempt> getAttempts(Long userId, Long topicId) {
        return examAttemptRepository.findByUserIdAndTopicIdOrderByCompletedAtDesc(userId, topicId);
    }

    public int getAttemptCount(Long userId, Long topicId) {
        return examAttemptRepository.countByUserIdAndTopicId(userId, topicId);
    }

    /**
     * Deserialises the answersJson of an attempt back into a Map<String, String>.
     */
    public Map<String, String> parseAnswers(ExamAttempt attempt) {
        if (attempt.getAnswersJson() == null) return Collections.emptyMap();
        try {
            return objectMapper.readValue(attempt.getAnswersJson(),
                    new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
