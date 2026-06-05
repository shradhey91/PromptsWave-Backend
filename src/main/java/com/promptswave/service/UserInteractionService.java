package com.promptswave.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.response.CopyHistoryResponse;
import com.promptswave.dto.response.InteractionStatusResponse;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.PromptSummaryResponse;
import com.promptswave.entity.Prompt;
import com.promptswave.entity.PromptCopyEvent;
import com.promptswave.entity.PromptLike;
import com.promptswave.entity.User;
import com.promptswave.entity.UserSavedPrompt;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.PromptCopyEventRepo;
import com.promptswave.repository.PromptLikeRepo;
import com.promptswave.repository.PromptRepo;
import com.promptswave.repository.UserRepo;
import com.promptswave.repository.UserSavedPromptRepo;

import java.util.List;

@Service
public class UserInteractionService {

    private final PromptLikeRepo promptLikeRepo;
    private final UserSavedPromptRepo userSavedPromptRepo;
    private final PromptCopyEventRepo promptCopyEventRepo;
    private final PromptRepo promptRepo;
    private final UserRepo userRepo;
    private final PromptService promptService;

    public UserInteractionService(
            PromptLikeRepo promptLikeRepo,
            UserSavedPromptRepo userSavedPromptRepo,
            PromptCopyEventRepo promptCopyEventRepo,
            PromptRepo promptRepo,
            UserRepo userRepo,
            PromptService promptService) {
        this.promptLikeRepo = promptLikeRepo;
        this.userSavedPromptRepo = userSavedPromptRepo;
        this.promptCopyEventRepo = promptCopyEventRepo;
        this.promptRepo = promptRepo;
        this.userRepo = userRepo;
        this.promptService = promptService;
    }

    public InteractionStatusResponse getStatus(Long userId, Long promptId) {
        Prompt prompt = getPublishedPrompt(promptId);

        boolean liked = promptLikeRepo.existsByUserIdAndPromptId(userId, promptId);
        boolean saved = userSavedPromptRepo.existsByUserIdAndPromptId(userId, promptId);

        return new InteractionStatusResponse(
                promptId,
                liked,
                saved,
                prompt.getLikesCount(),
                prompt.getTimesCopied()
        );
    }

    @Transactional
    public InteractionStatusResponse toggleLike(Long userId, Long promptId) {
        Prompt prompt = getPublishedPrompt(promptId);
        User user = getUser(userId);

        boolean alreadyLiked = promptLikeRepo.existsByUserIdAndPromptId(userId, promptId);

        if (alreadyLiked) {
            // Unlike
            promptLikeRepo.findByUserIdAndPromptId(userId, promptId)
                    .ifPresent(promptLikeRepo::delete);
            promptService.removeLike(promptId);
            prompt.setLikesCount(Math.max(0, prompt.getLikesCount() - 1));
        } else {
            // Like
            PromptLike like = PromptLike.builder()
                    .user(user)
                    .prompt(prompt)
                    .build();
            promptLikeRepo.save(like);
            promptService.addLike(promptId);
            prompt.setLikesCount(prompt.getLikesCount() + 1);
        }

        return new InteractionStatusResponse(
                promptId,
                !alreadyLiked,
                userSavedPromptRepo.existsByUserIdAndPromptId(userId, promptId),
                prompt.getLikesCount(),
                prompt.getTimesCopied()
        );
    }

    // SAVE / UNSAVE

    @Transactional
    public InteractionStatusResponse toggleSave(Long userId, Long promptId) {
        Prompt prompt = getPublishedPrompt(promptId);
        User user = getUser(userId);

        boolean alreadySaved = userSavedPromptRepo.existsByUserIdAndPromptId(userId, promptId);

        if (alreadySaved) {
            userSavedPromptRepo.findByUserIdAndPromptId(userId, promptId)
                    .ifPresent(userSavedPromptRepo::delete);
        } else {
            UserSavedPrompt saved = UserSavedPrompt.builder()
                    .user(user)
                    .prompt(prompt)
                    .build();
            userSavedPromptRepo.save(saved);
        }

        return new InteractionStatusResponse(
                promptId,
                promptLikeRepo.existsByUserIdAndPromptId(userId, promptId),
                !alreadySaved,
                prompt.getLikesCount(),
                prompt.getTimesCopied()
        );
    }


    @Transactional
    public void recordCopy(Long userId, Long promptId) {
        Prompt prompt = getPublishedPrompt(promptId);

        PromptCopyEvent event = PromptCopyEvent.builder()
                .prompt(prompt)
                .user(userId != null ? getUser(userId) : null)
                .build();

        promptCopyEventRepo.save(event);
        promptService.recordCopy(promptId);
    }

    // USER LIBRARY: LIKED PROMPTS

    public PagedResponse<PromptSummaryResponse> getLikedPrompts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likedAt"));
        Page<PromptLike> likes = promptLikeRepo.findByUserIdWithPrompt(userId, pageable);
        return PagedResponse.from(likes.map(like -> promptService.toSummaryResponse(like.getPrompt())));
    }

    // USER LIBRARY: SAVED PROMPTS

    public PagedResponse<PromptSummaryResponse> getSavedPrompts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "savedAt"));
        Page<UserSavedPrompt> saved = userSavedPromptRepo.findByUserIdWithPrompt(userId, pageable);
        return PagedResponse.from(saved.map(sp -> promptService.toSummaryResponse(sp.getPrompt())));
    }

   

    public PagedResponse<CopyHistoryResponse> getCopyHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "copiedAt"));
        Page<PromptCopyEvent> events = promptCopyEventRepo.findByUserIdWithPrompt(userId, pageable);
        return PagedResponse.from(events.map(this::toCopyHistoryResponse));
    }

    // HELPERS

    private Prompt getPublishedPrompt(Long promptId) {
        Prompt prompt = promptRepo.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found"));
        if (!prompt.getIsPublished()) {
            throw new ResourceNotFoundException("Prompt not found");
        }
        return prompt;
    }

    private User getUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CopyHistoryResponse toCopyHistoryResponse(PromptCopyEvent event) {
        Prompt p = event.getPrompt();
        List<String> aiNames = p.getRecommendedAiEntities().stream()
                .map(link -> link.getAiEntity().getName())
                .toList();
        return new CopyHistoryResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getImageUrl(),
                p.getCategory().getName(),
                p.getCategory().getSlug(),
                aiNames,
                event.getCopiedAt()
        );
    }
}

