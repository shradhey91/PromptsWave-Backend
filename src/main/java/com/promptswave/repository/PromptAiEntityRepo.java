package com.promptswave.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.entity.PromptAiEntity;

@Repository
public interface PromptAiEntityRepo extends JpaRepository<PromptAiEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PromptAiEntity p WHERE p.prompt.id = :promptId")
    void deleteByPromptId(Long promptId);
}
