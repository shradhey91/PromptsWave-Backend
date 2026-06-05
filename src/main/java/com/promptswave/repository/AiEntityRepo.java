package com.promptswave.repository;

import com.promptswave.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiEntityRepo extends JpaRepository<AiEntity, Long> {

    Optional<AiEntity> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<AiEntity> findByIsActiveTrueOrderByNameAsc();
}
