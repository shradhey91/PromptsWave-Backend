package com.promptswave.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promptswave.dto.request.RegisterRequest;
import com.promptswave.dto.response.AuthResponse;
import com.promptswave.dto.response.UserProfileResponse;
import com.promptswave.entity.EmailVerificationToken;
import com.promptswave.entity.PasswordResetToken;
import com.promptswave.entity.User;
import com.promptswave.enums.Role;
import com.promptswave.exception.AccessDeniedException;
import com.promptswave.exception.ConflictException;
import com.promptswave.exception.ResourceNotFoundException;
import com.promptswave.repository.EmailVerificationTokenRepo;
import com.promptswave.repository.PasswordResetTokenRepo;
import com.promptswave.repository.UserRepo;
import com.promptswave.security.JwtUtil;
import com.promptswave.security.TokenBlacklist;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final EmailVerificationTokenRepo emailTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(
            UserRepo userRepo,
            EmailVerificationTokenRepo emailTokenRepo,
            PasswordResetTokenRepo passwordResetTokenRepo,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            EmailService emailService,
            TokenBlacklist tokenBlacklist) {
        this.userRepo = userRepo;
        this.emailTokenRepo = emailTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.tokenBlacklist = tokenBlacklist;
    }

    //  REGISTER

    @Transactional
    public String register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepo.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .profileIconUrl(request.profileIconUrl())
                .country(request.country())
                .referralSource(request.referralSource())
                .role(Role.USER)
                .isEmailVerified(false)
                .isActive(true)
                .build();

        userRepo.save(user);

        sendVerificationToken(user);

        return "Registration successful. Please check your email to verify your account.";
    }

    // LOGIN
    
    public AuthResponse login(String email, String password) {
        User user = userRepo.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!user.getIsEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in.");
        }

        if (!user.getIsActive()) {
            throw new AccessDeniedException("Your account has been suspended. Please contact support.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(accessToken, refreshToken, toProfileResponse(user));
    }

    // LOGOUT
   

    public void logout(String accessToken) {
        tokenBlacklist.blacklist(accessToken, null);
    }

    // REFRESH TOKEN
    
    public Map<String, String> refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        return Map.of("accessToken", newAccessToken);
    }

    // VERIFY EMAIL

    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepo.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("This verification link has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification link has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        verificationToken.setUsed(true);

        userRepo.save(user);
        emailTokenRepo.save(verificationToken);

        return "Email verified successfully. You can now log in.";
    }

    // RESEND VERIFICATION EMAIL

    @Transactional
    public String resendVerificationEmail(String email) {
        User user = userRepo.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));

        if (user.getIsEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        emailTokenRepo.deleteByUserId(user.getId());
        sendVerificationToken(user);

        return "Verification email resent. Please check your inbox.";
    }

    // FORGOT PASSWORD

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepo.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));

        passwordResetTokenRepo.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        passwordResetTokenRepo.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return "Password reset link sent to your email.";
    }

    // RESET PASSWORD
    
    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);

        userRepo.save(user);
        passwordResetTokenRepo.save(resetToken);

        return "Password reset successfully. You can now log in.";
    }

    //  CHANGE PASSWORD

    public String changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        return "Password changed successfully.";
    }

    // CHANGE EMAIL
    
    @Transactional
    public String changeEmail(Long userId, String newEmail, String password) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AccessDeniedException("Password is incorrect");
        }

        String normalizedEmail = newEmail.toLowerCase().trim();

        if (userRepo.existsByEmail(normalizedEmail)) {
            throw new ConflictException("This email is already in use");
        }

        user.setEmail(normalizedEmail);
        user.setIsEmailVerified(false);
        userRepo.save(user);

        emailTokenRepo.deleteByUserId(userId);
        sendVerificationToken(user);

        return "Email updated. Please verify your new email address.";
    }

    // HELPERS

    private void sendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        emailTokenRepo.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileIconUrl(),
                user.getCountry(),
                user.getReferralSource(),
                user.getRole().name(),
                user.getIsEmailVerified(),
                user.getCreatedAt(),
                user.getLastLoginAt());
    }
}
