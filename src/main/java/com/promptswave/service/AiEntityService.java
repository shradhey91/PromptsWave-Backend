package com.promptswave.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.request.CreateAiEntityRequest;
import com.promptswave.dto.response.AiEntityResponse;
import com.promptswave.entity.AiEntity;
import com.promptswave.exception.ConflictException;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.AiEntityRepo;

import java.util.List;

@Service
public class AiEntityService {

    private final AiEntityRepo aiEntityRepo;

    public AiEntityService(AiEntityRepo aiEntityRepo) {
        this.aiEntityRepo = aiEntityRepo;
    }

    public List<AiEntityResponse> getAllActive() {
        return aiEntityRepo.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toResponse).toList();
    }

    public AiEntityResponse getById(Long id) {
        return toResponse(aiEntityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI entity not found")));
    }

    public List<AiEntityResponse> getAllForAdmin() {
        return aiEntityRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AiEntityResponse create(CreateAiEntityRequest request) {
        if (aiEntityRepo.existsByName(request.name())) {
            throw new ConflictException("AI entity '" + request.name() + "' already exists");
        }
        if (aiEntityRepo.existsBySlug(request.slug())) {
            throw new ConflictException("Slug '" + request.slug() + "' is already taken");
        }

        AiEntity entity = AiEntity.builder()
                .name(request.name().trim())
                .slug(request.slug().toLowerCase().trim())
                .iconUrl(request.iconUrl())
                .description(request.description())
                .websiteUrl(request.websiteUrl())
                .isActive(true)
                .build();

        return toResponse(aiEntityRepo.save(entity));
    }

    @Transactional
    public AiEntityResponse update(Long id, CreateAiEntityRequest request) {
        AiEntity entity = aiEntityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI entity not found"));

        entity.setName(request.name().trim());
        entity.setSlug(request.slug().toLowerCase().trim());
        if (request.iconUrl() != null)
            entity.setIconUrl(request.iconUrl());
        if (request.description() != null)
            entity.setDescription(request.description());
        if (request.websiteUrl() != null)
            entity.setWebsiteUrl(request.websiteUrl());

        return toResponse(aiEntityRepo.save(entity));
    }

    @Transactional
    public void toggleActive(Long id) {
        AiEntity entity = aiEntityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI entity not found"));
        entity.setIsActive(!entity.getIsActive());
        aiEntityRepo.save(entity);
    }

    public AiEntityResponse toResponse(AiEntity entity) {
        return new AiEntityResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getIconUrl(),
                entity.getDescription(),
                entity.getWebsiteUrl(),
                entity.getIsActive());
    }
}
