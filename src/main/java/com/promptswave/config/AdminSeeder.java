package com.promptswave.config;

import com.promptswave.entity.User;
import com.promptswave.enums.Role;
import com.promptswave.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@promptswave.com";
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .name("PromptsWave Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Adm!n@PromptsW4ve#2026"))
                    .role(Role.ADMIN)
                    .isEmailVerified(true)
                    .isActive(true)
                    .country("IN")
                    .build();
            userRepo.save(admin);
            System.out.println("Admin user created: " + adminEmail);
        }
    }
}
