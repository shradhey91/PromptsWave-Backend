package com.promptswave.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.request.CreateCategoryRequest;
import com.promptswave.dto.request.UpdateCategoryRequest;
import com.promptswave.dto.response.CategoryResponse;
import com.promptswave.dto.response.CategoryTreeResponse;
import com.promptswave.entity.Category;
import com.promptswave.exception.ConflictException;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.CategoryRepo;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    // PUBLIC 

    // Full tree: top-level categories each with their subcategories (for nav menus)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> topLevel = categoryRepo
                .findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

        return topLevel.stream().map(parent -> {
            List<CategoryResponse> subs = categoryRepo
                    .findByParentIdAndIsActiveTrueOrderBySortOrderAsc(parent.getId())
                    .stream()
                    .map(sub -> toResponse(sub))
                    .toList();

            long promptCount = categoryRepo.countPublishedPromptsByCategoryId(parent.getId());

            return new CategoryTreeResponse(
                    parent.getId(),
                    parent.getName(),
                    parent.getSlug(),
                    parent.getDescription(),
                    parent.getIconUrl(),
                    parent.getSortOrder(),
                    promptCount,
                    subs
            );
        }).toList();
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepo.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
        return toResponse(category);
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return toResponse(category);
    }

    // ADMIN 

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepo.existsBySlug(request.slug())) {
            throw new ConflictException("A category with slug '" + request.slug() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.name().trim())
                .slug(request.slug().toLowerCase().trim())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .isActive(true)
                .build();

        if (request.parentId() != null) {
            Category parent = categoryRepo.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));

            if (parent.getLevel() != 0) {
                throw new IllegalArgumentException(
                        "Subcategories can only be created under top-level categories");
            }

            category.setParent(parent);
            category.setLevel(1);
        } else {
            category.setLevel(0);
        }

        return toResponse(categoryRepo.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.name().trim());
        if (request.description() != null) category.setDescription(request.description());
        if (request.iconUrl() != null) category.setIconUrl(request.iconUrl());
        if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
        if (request.isActive() != null) category.setIsActive(request.isActive());

        return toResponse(categoryRepo.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        long promptCount = categoryRepo.countPublishedPromptsByCategoryId(id);
        if (promptCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete category with " + promptCount + " active prompts. " +
                    "Move or delete those prompts first.");
        }

        categoryRepo.delete(category);
        //category.setIsActive(false);
       // categoryRepo.save(category);
    }

    // All categories (including inactive) for admin management view
    public List<CategoryResponse> getAllForAdmin() {
        return categoryRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    public CategoryResponse toResponse(Category category) {
        long promptCount = categoryRepo.countPublishedPromptsByCategoryId(category.getId());
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getLevel(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                promptCount
        );
    }
}
