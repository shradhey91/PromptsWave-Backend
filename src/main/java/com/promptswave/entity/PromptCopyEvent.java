package com.promptswave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_copy_events", indexes = {
        @Index(name = "idx_copy_user", columnList = "user_id"),
        @Index(name = "idx_copy_prompt", columnList = "prompt_id"),
        @Index(name = "idx_copy_copied_at", columnList = "copiedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptCopyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    private LocalDateTime copiedAt;

    @PrePersist
    protected void onCreate() {
        copiedAt = LocalDateTime.now();
    }
}