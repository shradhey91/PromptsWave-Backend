package com.promptswave.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.response.AdminUserResponse;
import com.promptswave.dto.response.DashboardStatsResponse;
import com.promptswave.dto.response.PagedResponse;
import com.promptswave.dto.response.TopPromptResponse;
import com.promptswave.entity.Prompt;
import com.promptswave.entity.User;
import com.promptswave.enums.Role;
import com.promptswave.exception.AccessDeniedException;
import com.promptswave.exception.ConflictException;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.AiEntityRepo;
import com.promptswave.repository.CategoryRepo;
import com.promptswave.repository.PromptCopyEventRepo;
import com.promptswave.repository.PromptLikeRepo;
import com.promptswave.repository.PromptRepo;
import com.promptswave.repository.UserRepo;

import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepo userRepo;
    private final PromptRepo promptRepo;
    private final CategoryRepo categoryRepo;
    private final AiEntityRepo aiEntityRepo;
    private final PromptLikeRepo promptLikeRepo;
    private final PromptCopyEventRepo promptCopyEventRepo;

    public AdminDashboardService(
            UserRepo userRepo,
            PromptRepo promptRepo,
            CategoryRepo categoryRepo,
            AiEntityRepo aiEntityRepo,
            PromptLikeRepo promptLikeRepo,
            PromptCopyEventRepo promptCopyEventRepo) {
        this.userRepo = userRepo;
        this.promptRepo = promptRepo;
        this.categoryRepo = categoryRepo;
        this.aiEntityRepo = aiEntityRepo;
        this.promptLikeRepo = promptLikeRepo;
        this.promptCopyEventRepo = promptCopyEventRepo;
    }

    // DASHBOARD STATS

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepo.countByRole(Role.USER);
        long totalPrompts = promptRepo.count();
        long published = promptRepo.countByIsPublishedTrue();
        long drafts = totalPrompts - published;
        long totalCategories = categoryRepo.count();
        long totalAiEntities = aiEntityRepo.count();
        long totalLikes = promptLikeRepo.count();
        long totalCopies = promptCopyEventRepo.count();

        return new DashboardStatsResponse(
                totalUsers, totalPrompts, published, drafts,
                totalCategories, totalAiEntities, totalLikes, totalCopies);
    }

    // TOP PROMPTS

    public List<TopPromptResponse> getTopLikedPrompts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "likesCount"));
        return promptRepo.findByIsPublishedTrue(pageable)
                .stream().map(this::toTopPromptResponse).toList();
    }

    public List<TopPromptResponse> getTopCopiedPrompts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timesCopied"));
        return promptRepo.findByIsPublishedTrue(pageable)
                .stream().map(this::toTopPromptResponse).toList();
    }

    // USER MANAGEMENT

    public PagedResponse<AdminUserResponse> getAllUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepo.searchUsers(search.trim(), pageable);
        } else {
            users = userRepo.findAll(pageable);
        }

        return PagedResponse.from(users.map(this::toAdminUserResponse));
    }

    public AdminUserResponse getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toAdminUserResponse(user);
    }

    // Suspend a user (sets isActive = false)
    @Transactional
    public AdminUserResponse suspendUser(Long userId) {
        User user = getRegularUser(userId);
        if (!user.getIsActive()) {
            throw new ConflictException("User is already suspended");
        }
        user.setIsActive(false);
        return toAdminUserResponse(userRepo.save(user));
    }

    // Reactivate a suspended user
    @Transactional
    public AdminUserResponse reactivateUser(Long userId) {
        User user = getRegularUser(userId);
        if (user.getIsActive()) {
            throw new ConflictException("User is already active");
        }
        user.setIsActive(true);
        return toAdminUserResponse(userRepo.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getRegularUser(userId);
        userRepo.delete(user);
    }

    // HELPERS

    private User getRegularUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Admin accounts cannot be managed through this endpoint");
        }
        return user;
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        long likedCount = userRepo.countLikesByUserId(user.getId());
        long savedCount = userRepo.countSavedByUserId(user.getId());
        long copiesCount = userRepo.countCopiesByUserId(user.getId());

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCountry(),
                user.getReferralSource(),
                user.getRole().name(),
                user.getIsEmailVerified(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                likedCount,
                savedCount,
                copiesCount);
    }

    private TopPromptResponse toTopPromptResponse(Prompt p) {
        return new TopPromptResponse(
                p.getId(),
                p.getTitle(),
                p.getCategory().getName(),
                p.getImageUrl(),
                p.getLikesCount(),
                p.getTimesCopied());
    }
}
