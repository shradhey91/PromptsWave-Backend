package com.promptswave.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prompt_ai_entities", uniqueConstraints = @UniqueConstraint(columnNames = { "prompt_id",
        "ai_entity_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptAiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ai_entity_id", nullable = false)
    private AiEntity aiEntity;

    @Column(name = "rank_order", nullable = false)
    private Integer rank;
}
