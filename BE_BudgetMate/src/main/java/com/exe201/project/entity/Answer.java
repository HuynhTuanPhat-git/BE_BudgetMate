package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    String answer;
    
    @Column(name = "is_correct")
    boolean isCorrect;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    Question question;
}
