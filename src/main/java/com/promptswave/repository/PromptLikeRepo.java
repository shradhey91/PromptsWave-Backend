package com.promptswave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.PromptLike;

import java.util.Optional;

@Repository
public interface PromptLikeRepo extends JpaRepository<PromptLike, Long> {

    boolean existsByUserIdAndPromptId(Long userId, Long promptId);

    Optional<PromptLike> findByUserIdAndPromptId(Long userId, Long promptId);

    @Query("SELECT pl FROM PromptLike pl JOIN FETCH pl.prompt p WHERE pl.user.id = :userId AND p.isPublished = true")
    Page<PromptLike> findByUserIdWithPrompt(Long userId, Pageable pageable);

    long countByPromptId(Long promptId);

    @Modifying
    @Query("DELETE FROM PromptLike pl WHERE pl.prompt.id = :promptId")
    void deleteByPromptId(Long promptId);

    @Modifying
    @Query("DELETE FROM PromptLike pl WHERE pl.user.id = :userId")
    void deleteByUserId(Long userId);
}
