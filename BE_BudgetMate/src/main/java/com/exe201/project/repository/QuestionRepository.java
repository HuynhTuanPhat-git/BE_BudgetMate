package com.exe201.project.repository;

import com.exe201.project.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT q " +
            "FROM Question q " +
            "WHERE q.isActive = true AND q.id NOT IN :excludeIds " +
            "ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomActiveQuestionsExcluding(
            @Param("excludeIds") List<UUID> excludeIds,
            Pageable pageable
    );

    @Query("SELECT q FROM Question q " +
            "WHERE q.isActive = true " +
            "ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomActiveQuestions(Pageable pageable);

    @Query("SELECT q.id, q.questionText " +
            "FROM Question q " +
            "WHERE q.isActive = true")
    List<Object[]> findActiveQuestionProjections();

    @Query("SELECT DISTINCT q " +
            "FROM Question q LEFT JOIN FETCH q.answers a " +
            "WHERE q.id IN :questionIds ORDER BY q.id, a.displayOrder")
    List<Question> findQuestionsWithAnswersByIds(@Param("questionIds") List<UUID> questionIds);

    @Query("SELECT COUNT(q.id) " +
            "FROM Question q " +
            "WHERE q.isActive = true")
    Long countActiveQuestions();

    @Query("SELECT q " +
            "FROM Question q WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:isActive IS NULL OR q.isActive = :isActive)")
    Page<Question> findQuestionsWithFilters(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT q " +
            "FROM Question q LEFT JOIN FETCH q.answers " +
            "WHERE q.id = :id AND q.isActive = true")
    Question findByIdWithAnswers(@Param("id") UUID id);
}
