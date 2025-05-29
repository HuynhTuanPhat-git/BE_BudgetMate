package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @Column(name = "submitted_at")
    LocalDateTime submittedAt;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    Question question;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    
    @ManyToOne
    @JoinColumn(name = "answer_id")
    Answer answer;
}
