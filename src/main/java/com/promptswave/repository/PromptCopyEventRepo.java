package com.promptswave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.PromptCopyEvent;

import java.time.LocalDateTime;

@Repository
public interface PromptCopyEventRepo extends JpaRepository<PromptCopyEvent, Long> {

    long countByUserIdAndPromptId(Long userId, Long promptId);

    @Query("SELECT c FROM PromptCopyEvent c JOIN FETCH c.prompt p WHERE c.user.id = :userId AND p.isPublished = true")
    Page<PromptCopyEvent> findByUserIdWithPrompt(Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM PromptCopyEvent c WHERE c.copiedAt BETWEEN :from AND :to")
    long countCopiesInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT c.prompt.id, COUNT(c) as copies FROM PromptCopyEvent c GROUP BY c.prompt.id ORDER BY copies DESC")
    Page<Object[]> findTopCopiedPromptIds(Pageable pageable);

    @Modifying
    @Query("DELETE FROM PromptCopyEvent c WHERE c.prompt.id = :promptId")
    void deleteByPromptId(Long promptId);

    @Modifying
    @Query("DELETE FROM PromptCopyEvent c WHERE c.user.id = :userId")
    void deleteByUserId(Long userId);
}