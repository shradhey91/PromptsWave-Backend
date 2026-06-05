package com.promptswave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.entity.Prompt;

@Repository
public interface PromptRepo extends JpaRepository<Prompt, Long> {

    Page<Prompt> findByIsPublishedTrue(Pageable pageable);

    Page<Prompt> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Prompt p
            WHERE p.isPublished = true
              AND (p.category.id = :categoryId OR p.category.parent.id = :categoryId)
            """)
    Page<Prompt> findByCategoryOrSubcategoryAndPublished(
            @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Prompt p
            JOIN p.recommendedAiEntities pai
            WHERE p.isPublished = true AND pai.aiEntity.id = :aiEntityId
            """)
    Page<Prompt> findByRecommendedAiEntityAndPublished(
            @Param("aiEntityId") Long aiEntityId, Pageable pageable);

    @Query("""
            SELECT p FROM Prompt p
            WHERE p.isPublished = true
              AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.promptText) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Prompt> searchPublished(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT p FROM Prompt p
            WHERE p.isPublished = true
              AND (p.category.id = :categoryId OR p.category.parent.id = :categoryId)
              AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.promptText) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Prompt> searchPublishedInCategory(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Prompt p SET p.timesCopied = p.timesCopied + 1 WHERE p.id = :id")
    void incrementTimesCopied(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Prompt p SET p.likesCount = p.likesCount + 1 WHERE p.id = :id")
    void incrementLikesCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Prompt p SET p.likesCount = p.likesCount - 1 WHERE p.id = :id AND p.likesCount > 0")
    void decrementLikesCount(@Param("id") Long id);

    // ── Admin queries (all prompts including unpublished) ─────────────────────

    Page<Prompt> findAll(Pageable pageable);

    Page<Prompt> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Prompt p
            WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Prompt> searchAll(@Param("query") String query, Pageable pageable);

    long countByIsPublishedTrue();
}