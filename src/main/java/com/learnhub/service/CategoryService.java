package com.learnhub.service;

import com.learnhub.dto.CategoryDto;
import com.learnhub.entity.Category;
import com.learnhub.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for category-related business logic.
 * Provides CRUD operations used by both admin and public controllers.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all categories from the database.
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Find a category by its ID. Throws exception if not found.
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    /**
     * Create a new category from a DTO.
     */
    public Category create(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryRepository.save(category);
    }

    /**
     * Update an existing category.
     */
    public Category update(Long id, CategoryDto dto) {
        Category category = findById(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryRepository.save(category);
    }

    /**
     * Delete a category by ID. This also deletes all related topics and articles
     * because of CascadeType.ALL in the entity.
     */
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
