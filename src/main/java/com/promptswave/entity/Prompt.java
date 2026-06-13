package com.promptswave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prompts", indexes = {
        @Index(name = "idx_prompt_category", columnList = "category_id"),
        @Index(name = "idx_prompt_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_prompt_published", columnList = "isPublished"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(length = 1024)
    private String imageUrl;

    @Column(nullable = false)
    private Integer timesCopied;

    @Column(nullable = false)
    private Integer likesCount;

    @Column(nullable = false)
    private Boolean isPublished;

    @Column(nullable = false)
    private Boolean isPinnedToHero;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    @Builder.Default
    private List<PromptAiEntity> recommendedAiEntities = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (timesCopied == null)
            timesCopied = 0;
        if (likesCount == null)
            likesCount = 0;
        if (isPublished == null)
            isPublished = false;
        if (isPinnedToHero == null)
            isPinnedToHero = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}