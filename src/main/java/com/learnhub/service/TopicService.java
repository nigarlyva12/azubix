package com.learnhub.service;

import com.learnhub.dto.TopicDto;
import com.learnhub.entity.Category;
import com.learnhub.entity.Topic;
import com.learnhub.repository.CategoryRepository;
import com.learnhub.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for topic-related business logic.
 */
@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;

    public TopicService(TopicRepository topicRepository, CategoryRepository categoryRepository) {
        this.topicRepository = topicRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    public Topic findById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
    }

    /**
     * Find all topics that belong to a specific category.
     */
    public List<Topic> findByCategoryId(Long categoryId) {
        return topicRepository.findByCategoryId(categoryId);
    }

    /**
     * Count total topics in a category (needed for progress bar calculation).
     */
    public long countByCategoryId(Long categoryId) {
        return topicRepository.countByCategoryId(categoryId);
    }

    /**
     * Create a new topic. Looks up the category by ID from the DTO.
     */
    public Topic create(TopicDto dto) {
        // Find the parent category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Topic topic = new Topic();
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setCategory(category);

        return topicRepository.save(topic);
    }

    /**
     * Update an existing topic.
     */
    public Topic update(Long id, TopicDto dto) {
        Topic topic = findById(id);

        // If category changed, look up the new one
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setCategory(category);

        return topicRepository.save(topic);
    }

    public void delete(Long id) {
        topicRepository.deleteById(id);
    }
}
