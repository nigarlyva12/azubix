package com.learnhub.controller;

import com.learnhub.entity.*;
import com.learnhub.service.ExamService;
import com.learnhub.service.TopicService;
import com.learnhub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ExamController {

    private final ExamService  examService;
    private final TopicService topicService;
    private final UserService  userService;

    public ExamController(ExamService examService,
                          TopicService topicService,
                          UserService userService) {
        this.examService  = examService;
        this.topicService = topicService;
        this.userService  = userService;
    }

    @GetMapping("/topic/{topicId}/exam")
    public String showExam(@PathVariable Long topicId, Model model, Authentication auth) {
        Topic topic = topicService.findById(topicId);
        List<Question> questions = examService.getQuestionsForTopic(topicId);

  
        Map<Long, List<String>> optionsMap = new LinkedHashMap<>();
        for (Question q : questions) {
            optionsMap.put(q.getId(), examService.parseOptions(q));
        }

        model.addAttribute("topic",      topic);
        model.addAttribute("questions",  questions);
        model.addAttribute("optionsMap", optionsMap);

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                examService.getBestAttempt(user.getId(), topicId)
                           .ifPresent(a -> model.addAttribute("bestAttempt", a));
                model.addAttribute("attemptCount",
                        examService.getAttemptCount(user.getId(), topicId));
                model.addAttribute("recentAttempts",
                        examService.getAttempts(user.getId(), topicId));
            }
        }

        return "exam";
    }


    @PostMapping("/topic/{topicId}/exam/submit")
    public String submitExam(@PathVariable Long topicId,
                             @RequestParam Map<String, String> allParams,
                             Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Topic topic = topicService.findById(topicId);

        Map<String, String> answers = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().matches("\\d+")) {
                answers.put(entry.getKey(), entry.getValue());
            }
        }

        ExamAttempt attempt = examService.submitExam(user, topic, answers);
        return "redirect:/topic/" + topicId + "/exam/result/" + attempt.getId();
    }

    @GetMapping("/topic/{topicId}/exam/result/{attemptId}")
    public String showResult(@PathVariable Long topicId,
                             @PathVariable Long attemptId,
                             Model model,
                             Authentication auth) {
        ExamAttempt attempt = examService.findAttemptById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        Topic topic = topicService.findById(topicId);

        List<Question> questions = examService.getQuestionsForTopic(topicId);
        Map<String, String> userAnswers = examService.parseAnswers(attempt);

        List<Map<String, Object>> resultRows = new ArrayList<>();
        for (Question q : questions) {
            String userAnswer  = userAnswers.getOrDefault(String.valueOf(q.getId()), "");
            List<String> opts  = examService.parseOptions(q);
            boolean correct    = isCorrect(q, userAnswer);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("question",    q);
            row.put("options",     opts);
            row.put("userAnswer",  userAnswer);
            row.put("correct",     correct);
            row.put("userLabel",   label(q, userAnswer, opts));
            row.put("correctLabel",label(q, q.getCorrectAnswer(), opts));
            resultRows.add(row);
        }

        model.addAttribute("topic",      topic);
        model.addAttribute("attempt",    attempt);
        model.addAttribute("resultRows", resultRows);

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                examService.getBestAttempt(user.getId(), topicId)
                           .ifPresent(a -> model.addAttribute("bestAttempt", a));
            }
        }

        return "exam-result";
    }

    private boolean isCorrect(Question q, String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) return false;
        String correct = q.getCorrectAnswer().trim().toLowerCase();
        String answer  = userAnswer.trim().toLowerCase();
        if (q.getType() == Question.QuestionType.FILL_BLANK) {
            return correct.equals(answer) || correct.contains(answer) || answer.contains(correct);
        }
        return correct.equals(answer);
    }

    private String label(Question q, String value, List<String> options) {
        if (value == null || value.isBlank()) return "—";
        if (q.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
            try {
                int idx = Integer.parseInt(value);
                if (idx >= 0 && idx < options.size()) return options.get(idx);
            } catch (NumberFormatException ignored) {}
        }
        return value;
    }
}
