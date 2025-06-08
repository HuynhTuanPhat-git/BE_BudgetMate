package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_logs", indexes = {
        @Index(name = "idx_quiz_log_user_date", columnList = "user_id, quiz_date"),
        @Index(name = "idx_quiz_log_user_correct", columnList = "user_id, is_correct"),
        @Index(name = "idx_quiz_log_question_user", columnList = "question_id, user_id"),
        @Index(name = "idx_quiz_log_date", columnList = "quiz_date"),
        @Index(name = "idx_quiz_log_submitted", columnList = "submitted_at")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class QuizLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    LocalDateTime submittedAt;

    @Column(name = "quiz_date", nullable = false)
    LocalDate quizDate;

    @Column(name = "is_correct", nullable = false)
    Boolean isCorrect;

    @Builder.Default
    @Column(name = "credits_earned")
    Integer creditsEarned = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    Answer answer;
}