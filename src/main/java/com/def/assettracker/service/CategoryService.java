
package com.def.assettracker.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.def.assettracker.entity.Category;
import com.def.assettracker.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));
    }

    public Category create(Category category) {
        if (category.getName() == null ||
                category.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category name is required");
        }

        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists");
        }

        return categoryRepository.save(category);
    }

    public Category update(Long id, Category input) {
        Category existing = getById(id);

        if (input.getName() == null || input.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category name is required");
        }

        boolean nameUsed = categoryRepository
                .existsByNameIgnoreCase(input.getName());

        if (nameUsed &&
                !existing.getName().equalsIgnoreCase(input.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists");
        }

        existing.setName(input.getName());
        existing.setDescription(input.getDescription());

        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        Category category = getById(id);

        try {
            categoryRepository.delete(category);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this category because it is linked to assets. "
                            + "Reassign or delete the linked assets first."
            );
        }
    }
}
