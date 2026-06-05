package com.promptswave.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.promptswave.entity.UserSavedPrompt;

import java.util.Optional;

@Repository
public interface UserSavedPromptRepo extends JpaRepository<UserSavedPrompt, Long> {

    boolean existsByUserIdAndPromptId(Long userId, Long promptId);

    Optional<UserSavedPrompt> findByUserIdAndPromptId(Long userId, Long promptId);

    @Query("SELECT sp FROM UserSavedPrompt sp JOIN FETCH sp.prompt p WHERE sp.user.id = :userId AND p.isPublished = true")
    Page<UserSavedPrompt> findByUserIdWithPrompt(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}