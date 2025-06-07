package com.exe201.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "answers", indexes = {
        @Index(name = "idx_answer_question", columnList = "question_id"),
        @Index(name = "idx_answer_correct", columnList = "is_correct"),
        @Index(name = "idx_answer_display_order", columnList = "display_order"),
        @Index(name = "idx_answer_question_correct", columnList = "question_id, is_correct")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotBlank(message = "Answer text should not be blank")
    @Size(max = 500, message = "Answer text should not exceed 500 characters")
    @Column(name = "answer_text", nullable = false, length = 500)
    String answerText;

    @Column(name = "is_correct", nullable = false)
    Boolean isCorrect = false;

    @Column(name = "display_order")
    Integer displayOrder = 1;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;
}
