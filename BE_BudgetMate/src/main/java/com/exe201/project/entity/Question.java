package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    String type;
    
    String question;
    
    @OneToMany(mappedBy = "question")
    List<Answer> answers;
    
    @OneToMany(mappedBy = "question")
    List<QuizLog> quizLogs;
}
