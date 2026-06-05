package com.promptswave.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    List<Category> findByParentIsNullOrderBySortOrderAsc();

    List<Category> findByIsActiveTrueOrderByLevelAscSortOrderAsc();

    @Query("SELECT COUNT(p) FROM Prompt p WHERE p.category.id = :categoryId AND p.isPublished = true")
    long countPublishedPromptsByCategoryId(Long categoryId);
}
