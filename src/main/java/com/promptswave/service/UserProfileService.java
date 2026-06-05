package com.promptswave.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.response.UserProfileResponse;
import com.promptswave.entity.User;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.UserRepo;

@Service
public class UserProfileService {

    private final UserRepo userRepo;
    private final AuthService authService;

    public UserProfileService(UserRepo userRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.authService = authService;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return authService.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, String name, String profileIconUrl, String country) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        user.setName(name.trim());

        if (profileIconUrl != null) {
            user.setProfileIconUrl(profileIconUrl.isBlank() ? null : profileIconUrl.trim());
        }

        if (country != null) {
            user.setCountry(country.trim());
        }

        userRepo.save(user);
        return authService.toProfileResponse(user);
    }

    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        user.setEmail("deleted_" + userId + "_" + user.getEmail());
        userRepo.save(user);
    }
}
