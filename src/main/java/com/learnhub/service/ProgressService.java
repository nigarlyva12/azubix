package com.learnhub.service;

import com.learnhub.entity.Progress;
import com.learnhub.entity.Topic;
import com.learnhub.entity.User;
import com.learnhub.repository.ProgressRepository;
import com.learnhub.repository.TopicRepository;
import com.learnhub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for progress tracking.
 * Handles marking topics as complete and calculating progress percentages.
 */
@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public ProgressService(ProgressRepository progressRepository,
                           UserRepository userRepository,
                           TopicRepository topicRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
    }

    /**
     * Toggle the completion status of a topic for a user.
     * If no progress record exists, create one and mark as completed.
     * If a record exists, toggle the completed flag.
     */
    public void toggleTopicCompletion(Long userId, Long topicId) {
        Optional<Progress> existingProgress =
                progressRepository.findByUserIdAndTopicId(userId, topicId);

        if (existingProgress.isPresent()) {
            // Toggle: if completed -> uncomplete, and vice versa
            Progress progress = existingProgress.get();
            progress.setCompleted(!progress.isCompleted());
            progressRepository.save(progress);
        } else {
            // First time marking - create new record as completed
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new RuntimeException("Topic not found"));

            Progress progress = new Progress(user, topic, true);
            progressRepository.save(progress);
        }
    }

    /**
     * Check if a specific topic is completed by a user.
     */
    public boolean isTopicCompleted(Long userId, Long topicId) {
        return progressRepository.findByUserIdAndTopicId(userId, topicId)
                .map(Progress::isCompleted)
                .orElse(false);
    }

    /**
     * Get all completed topic IDs for a user.
     * Useful for batch-checking completion status in templates.
     */
    public List<Long> getCompletedTopicIds(Long userId) {
        return progressRepository.findCompletedTopicIdsByUserId(userId);
    }

    /**
     * Calculate the progress percentage for a user in a specific category.
     * Returns a value between 0 and 100.
     *
     * Formula: (completed topics in category / total topics in category) * 100
     */
    public int calculateCategoryProgress(Long userId, Long categoryId) {
        long totalTopics = topicRepository.countByCategoryId(categoryId);

        // Avoid division by zero if category has no topics
        if (totalTopics == 0) {
            return 0;
        }

        long completedTopics = progressRepository
                .countCompletedByUserAndCategory(userId, categoryId);

        return (int) ((completedTopics * 100) / totalTopics);
    }
}
