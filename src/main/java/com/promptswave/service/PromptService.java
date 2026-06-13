package com.promptswave.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.request.CreatePromptRequest;
import com.promptswave.dto.request.UpdatePromptRequest;
import com.promptswave.dto.response.CategoryResponse;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.PromptResponse;
import com.promptswave.dto.response.PromptSummaryResponse;
import com.promptswave.entity.AiEntity;
import com.promptswave.entity.Category;
import com.promptswave.entity.Prompt;
import com.promptswave.entity.PromptAiEntity;
import com.promptswave.entity.User;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.AiEntityRepo;
import com.promptswave.repository.CategoryRepo;
import com.promptswave.repository.PromptAiEntityRepo;
import com.promptswave.repository.PromptCopyEventRepo;
import com.promptswave.repository.PromptLikeRepo;
import com.promptswave.repository.PromptRepo;
import com.promptswave.repository.UserRepo;
import com.promptswave.repository.UserSavedPromptRepo;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptService {

    private final PromptRepo promptRepo;
    private final CategoryRepo categoryRepo;
    private final AiEntityRepo aiEntityRepo;
    private final PromptAiEntityRepo promptAiEntityRepo;
    private final UserRepo userRepo;
    private final PromptLikeRepo promptLikeRepo;
    private final UserSavedPromptRepo userSavedPromptRepo;
    private final PromptCopyEventRepo promptCopyEventRepo;

    public PromptService(
            PromptRepo promptRepo,
            CategoryRepo categoryRepo,
            AiEntityRepo aiEntityRepo,
            PromptAiEntityRepo promptAiEntityRepo,
            UserRepo userRepo,
            PromptLikeRepo promptLikeRepo,
            UserSavedPromptRepo userSavedPromptRepo,
            PromptCopyEventRepo promptCopyEventRepo) {
        this.promptRepo = promptRepo;
        this.categoryRepo = categoryRepo;
        this.aiEntityRepo = aiEntityRepo;
        this.promptAiEntityRepo = promptAiEntityRepo;
        this.userRepo = userRepo;
        this.promptLikeRepo = promptLikeRepo;
        this.userSavedPromptRepo = userSavedPromptRepo;
        this.promptCopyEventRepo = promptCopyEventRepo;
    }

    public PagedResponse<PromptSummaryResponse> browsePrompts(
            String sort,
            Long categoryId,
            Long aiEntityId,
            String search,
            int page,
            int size) {

        Pageable pageable = buildPageable(sort, page, size);
        Page<Prompt> result;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategoryFilter = categoryId != null;
        boolean hasAiFilter = aiEntityId != null;

        if (hasSearch && hasCategoryFilter) {
            result = promptRepo.searchPublishedInCategoryIds(
                    search.trim(), resolveCategorySubtreeIds(categoryId), pageable);
        } else if (hasSearch) {
            result = promptRepo.searchPublished(search.trim(), pageable);
        } else if (hasCategoryFilter) {
            result = promptRepo.findByCategoryIdsAndPublished(
                    resolveCategorySubtreeIds(categoryId), pageable);
        } else if (hasAiFilter) {
            result = promptRepo.findByRecommendedAiEntityAndPublished(aiEntityId, pageable);
        } else {
            result = promptRepo.findByIsPublishedTrue(pageable);
        }

        return PagedResponse.from(result.map(this::toSummaryResponse));
    }

    private List<Long> resolveCategorySubtreeIds(Long rootId) {
        List<Long> ids = new ArrayList<>();
        java.util.Deque<Long> stack = new java.util.ArrayDeque<>();
        stack.push(rootId);
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            ids.add(id);
            categoryRepo.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(id)
                    .forEach(child -> stack.push(child.getId()));
        }
        return ids;
    }

    public PromptResponse getPublishedPromptById(Long id) {
        Prompt prompt = promptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));

        if (!prompt.getIsPublished()) {
            throw new ResourceNotFoundException("Prompt not found");
        }

        return toResponse(prompt);
    }

    // ADMIN CRUD

    public PagedResponse<PromptResponse> getAllPromptsForAdmin(
            String search, Long categoryId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Prompt> result;

        if (search != null && !search.isBlank()) {
            result = promptRepo.searchAll(search.trim(), pageable);
        } else if (categoryId != null) {
            result = promptRepo.findByCategoryId(categoryId, pageable);
        } else {
            result = promptRepo.findAll(pageable);
        }

        return PagedResponse.from(result.map(this::toResponse));
    }

    public PromptResponse getPromptByIdForAdmin(Long id) {
        Prompt prompt = promptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));
        return toResponse(prompt);
    }

    @Transactional
    public PromptResponse createPrompt(CreatePromptRequest request, Long adminUserId) {
        Category category = categoryRepo.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Prompt prompt = Prompt.builder()
                .title(request.title().trim())
                .promptText(request.promptText().trim())
                .description(request.description())
                .category(category)
                .uploadedBy(admin)
                .imageUrl(request.imageUrl())
                .isPublished(request.publish() != null ? request.publish() : false)
                .timesCopied(0)
                .likesCount(0)
                .build();

        prompt = promptRepo.save(prompt);

        List<PromptAiEntity> aiLinks = buildAiLinks(prompt, request.recommendedAiEntityIds());
        promptAiEntityRepo.saveAll(aiLinks);
        prompt.setRecommendedAiEntities(aiLinks);

        return toResponse(prompt);
    }

    @Transactional
    public PromptResponse updatePrompt(Long id, UpdatePromptRequest request) {
        Prompt prompt = promptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));

        Category category = categoryRepo.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        prompt.setTitle(request.title().trim());
        prompt.setPromptText(request.promptText().trim());
        prompt.setDescription(request.description());
        prompt.setCategory(category);
        if (request.imageUrl() != null)
            prompt.setImageUrl(request.imageUrl());
        if (request.isPublished() != null)
            prompt.setIsPublished(request.isPublished());

        prompt.getRecommendedAiEntities().clear();
        promptRepo.saveAndFlush(prompt);

        List<PromptAiEntity> aiLinks = buildAiLinks(prompt, request.recommendedAiEntityIds());
        prompt.getRecommendedAiEntities().addAll(aiLinks);

        return toResponse(promptRepo.save(prompt));
    }

    @Transactional
    public PromptResponse togglePublish(Long id) {
        Prompt prompt = promptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));
        prompt.setIsPublished(!prompt.getIsPublished());
        return toResponse(promptRepo.save(prompt));
    }

    /** Admin: pin/unpin a prompt to the hero section. */
    @Transactional
    public PromptResponse toggleHeroPin(Long id) {
        Prompt prompt = promptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));
        prompt.setIsPinnedToHero(!Boolean.TRUE.equals(prompt.getIsPinnedToHero()));
        return toResponse(promptRepo.save(prompt));
    }

    
    public List<PromptSummaryResponse> getHeroPrompts(int limit, String sortBy) {
        if (limit <= 0)
            limit = 6;

        List<Prompt> pinned = promptRepo.findByIsPinnedToHeroTrueAndIsPublishedTrueOrderByUpdatedAtDesc();

        List<Prompt> hero = new ArrayList<>(
                pinned.size() > limit ? pinned.subList(0, limit) : pinned);

        int remaining = limit - hero.size();
        if (remaining > 0) {
            List<Long> excludeIds = new ArrayList<>(hero.stream().map(Prompt::getId).toList());
            
            if (excludeIds.isEmpty())
                excludeIds.add(-1L);

            Pageable topN = PageRequest.of(0, remaining);
            List<Prompt> fillers = "copied".equalsIgnoreCase(sortBy)
                    ? promptRepo.findTopByCopiesExcluding(excludeIds, topN)
                    : promptRepo.findTopByLikesExcluding(excludeIds, topN);
            hero.addAll(fillers);
        }

        return hero.stream().map(this::toSummaryResponse).toList();
    }

    @Transactional
    public void deletePrompt(Long id) {
        if (!promptRepo.existsById(id)) {
            throw new ResourceNotFoundException("Prompt not found");
        }
        promptLikeRepo.deleteByPromptId(id);
        userSavedPromptRepo.deleteByPromptId(id);
        promptCopyEventRepo.deleteByPromptId(id);
        promptRepo.deleteById(id);
    }

    // INTERACTION HOOKS

    @Transactional
    public void recordCopy(Long promptId) {
        promptRepo.incrementTimesCopied(promptId);
    }

    @Transactional
    public void addLike(Long promptId) {
        promptRepo.incrementLikesCount(promptId);
    }

    @Transactional
    public void removeLike(Long promptId) {
        promptRepo.decrementLikesCount(promptId);
    }

    // HELPERS

    private Pageable buildPageable(String sort, int page, int size) {
        Sort sorting = switch (sort != null ? sort : "newest") {
            case "mostLiked" -> Sort.by(Sort.Direction.DESC, "likesCount");
            case "mostUsed" -> Sort.by(Sort.Direction.DESC, "timesCopied");
            case "trending" -> Sort.by(
                    Sort.Order.desc("likesCount"),
                    Sort.Order.desc("timesCopied"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return PageRequest.of(page, size, sorting);
    }

    private List<PromptAiEntity> buildAiLinks(Prompt prompt, List<Long> aiEntityIds) {
        List<PromptAiEntity> links = new ArrayList<>();
        for (int i = 0; i < aiEntityIds.size(); i++) {
            Long aiId = aiEntityIds.get(i);
            AiEntity aiEntity = aiEntityRepo.findById(aiId)
                    .orElseThrow(() -> new ResourceNotFoundException("AI entity not found: " + aiId));
            links.add(PromptAiEntity.builder()
                    .prompt(prompt)
                    .aiEntity(aiEntity)
                    .rank(i + 1)
                    .build());
        }
        return links;
    }

    // MAPPERS

    public PromptResponse toResponse(Prompt prompt) {
        List<PromptResponse.RankedAiEntityResponse> aiResponses = prompt
                .getRecommendedAiEntities().stream()
                .map(link -> new PromptResponse.RankedAiEntityResponse(
                        link.getAiEntity().getId(),
                        link.getAiEntity().getName(),
                        link.getAiEntity().getSlug(),
                        link.getAiEntity().getIconUrl(),
                        link.getRank()))
                .toList();

        CategoryResponse categoryResp = new CategoryResponse(
                prompt.getCategory().getId(),
                prompt.getCategory().getName(),
                prompt.getCategory().getSlug(),
                prompt.getCategory().getDescription(),
                prompt.getCategory().getIconUrl(),
                prompt.getCategory().getLevel(),
                prompt.getCategory().getSortOrder(),
                prompt.getCategory().getIsActive(),
                prompt.getCategory().getParent() != null
                        ? prompt.getCategory().getParent().getId()
                        : null,
                prompt.getCategory().getParent() != null
                        ? prompt.getCategory().getParent().getName()
                        : null,
                0L);

        return new PromptResponse(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getPromptText(),
                prompt.getDescription(),
                prompt.getImageUrl(),
                categoryResp,
                aiResponses,
                prompt.getTimesCopied(),
                prompt.getLikesCount(),
                prompt.getIsPublished(),
                prompt.getIsPinnedToHero(),
                prompt.getUploadedBy().getName(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt());
    }

    public PromptSummaryResponse toSummaryResponse(Prompt prompt) {
        List<String> aiNames = prompt.getRecommendedAiEntities().stream()
                .map(link -> link.getAiEntity().getName())
                .toList();

        return new PromptSummaryResponse(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getDescription(),
                prompt.getImageUrl(),
                prompt.getCategory().getName(),
                prompt.getCategory().getSlug(),
                aiNames,
                prompt.getTimesCopied(),
                prompt.getLikesCount(),
                prompt.getCreatedAt());
    }
}