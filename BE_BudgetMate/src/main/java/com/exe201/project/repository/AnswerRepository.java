package com.exe201.project.repository;

import com.exe201.project.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId ORDER BY a.displayOrder")
    List<Answer> findByQuestionIdOrderByDisplayOrder(@Param("questionId") UUID questionId);

    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId AND a.isCorrect = true")
    Optional<Answer> findCorrectAnswerByQuestionId(@Param("questionId") UUID questionId);
}