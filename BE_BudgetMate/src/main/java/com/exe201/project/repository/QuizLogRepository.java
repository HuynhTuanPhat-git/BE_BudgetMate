package com.exe201.project.repository;

import com.exe201.project.entity.QuizLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface QuizLogRepository extends JpaRepository<QuizLog, UUID> {

    @Query("SELECT COUNT(ql.id) " +
            "FROM QuizLog ql " +
            "WHERE ql.user.id = :userId AND ql.quizDate = :date")
    Long countByUserIdAndQuizDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(ql.id) " +
            "FROM QuizLog ql " +
            "WHERE ql.user.id = :userId AND ql.quizDate = :date AND ql.isCorrect = true")
    Long countCorrectAnswersByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(ql.creditsEarned), 0) " +
            "FROM QuizLog ql " +
            "WHERE ql.user.id = :userId AND ql.quizDate = :date")
    Integer sumPointsEarnedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT ql.question.id " +
            "FROM QuizLog ql " +
            "WHERE ql.user.id = :userId AND ql.quizDate = :date")
    List<UUID> findQuestionIdsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(ql.id) > 0 " +
            "THEN true ELSE false END " +
            "FROM QuizLog ql " +
            "WHERE ql.user.id = :userId AND ql.question.id = :questionId AND ql.quizDate = :date")
    boolean existsByUserIdAndQuestionIdAndQuizDate(
            @Param("userId") Long userId,
            @Param("questionId") UUID questionId,
            @Param("date") LocalDate date
    );

    @Query("SELECT new map(" +
            "CAST(COUNT(ql.id) as long) as totalQuizzes, " +
            "CAST(SUM(CASE WHEN ql.isCorrect = true THEN 1 ELSE 0 END) as long) as correctAnswers, " +
            "CAST(COALESCE(SUM(ql.creditsEarned), 0) as int) as totalCredits) " +
            "FROM QuizLog ql WHERE ql.user.id = :userId AND ql.quizDate = :date")
    Map<String, Object> getDailyQuizSummary(@Param("userId") Long userId, @Param("date") LocalDate date);
}