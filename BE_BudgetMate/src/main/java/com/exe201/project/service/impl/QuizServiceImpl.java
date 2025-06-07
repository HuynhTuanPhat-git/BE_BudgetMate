package com.exe201.project.service.impl;

import com.exe201.project.dto.request.question.SubmitQuizRequest;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.dto.response.quiz.DailyQuizStatusResponse;
import com.exe201.project.dto.response.quiz.QuizResultResponse;
import com.exe201.project.entity.Answer;
import com.exe201.project.entity.Question;
import com.exe201.project.entity.QuizLog;
import com.exe201.project.entity.User;
import com.exe201.project.exception.quiz.QuizLimitExceededException;
import com.exe201.project.exception.quiz.QuizNotFoundException;
import com.exe201.project.mapper.QuestionMapper;
import com.exe201.project.mapper.QuizMapper;
import com.exe201.project.repository.AnswerRepository;
import com.exe201.project.repository.QuestionRepository;
import com.exe201.project.repository.QuizLogRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImpl implements QuizService {

    private static final int DAILY_QUIZ_LIMIT = 3;
    private static final int PERFECT_SCORE_CREDITS = 2;
    private final QuestionRepository questionRepository;
    private final QuizLogRepository quizLogRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final QuizMapper quizMapper;

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getDailyQuizQuestions(Long userId) {
        log.info("Getting daily quiz questions for user: {}", userId);

        DailyQuizStatusResponse status = getDailyQuizStatus(userId);
        if (Boolean.FALSE.equals(status.canTakeQuiz())) {
            throw new QuizLimitExceededException("Daily quiz limit reached. You can take " + DAILY_QUIZ_LIMIT + " quizzes per day.");
        }

        LocalDate today = LocalDate.now();
        List<UUID> answeredQuestionIds = quizLogRepository.findQuestionIdsByUserAndDate(userId, today);

        List<Question> questions;
        int remainingQuestions = status.remainingQuizzes();

        if (answeredQuestionIds.isEmpty()) {
            questions = questionRepository.findRandomActiveQuestions(
                    PageRequest.of(0, remainingQuestions)
            );
        } else {
            questions = questionRepository.findRandomActiveQuestionsExcluding(
                    answeredQuestionIds,
                    PageRequest.of(0, remainingQuestions)
            );
        }

        if (questions.isEmpty()) {
            throw new QuizNotFoundException("No available questions found for today's quiz");
        }

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();
        List<Question> questionsWithAnswers = questionRepository.findQuestionsWithAnswersByIds(questionIds);

        return questionsWithAnswers.stream()
                .map(questionMapper::toQuizQuestionResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuizAnswer(Long userId, SubmitQuizRequest request) {
        log.info("Submitting quiz answer for user: {}, question: {}, answer: {}",
                userId, request.questionId(), request.answerId());

        LocalDate today = LocalDate.now();

        Map<String, Object> dailySummary = quizLogRepository.getDailyQuizSummary(userId, today);
        Long todayQuizCount = (Long) dailySummary.get("totalQuizzes");

        if (todayQuizCount >= DAILY_QUIZ_LIMIT) {
            throw new QuizLimitExceededException("Daily quiz limit exceeded");
        }

        // Check if user already answered this question today
        boolean alreadyAnswered = quizLogRepository.existsByUserIdAndQuestionIdAndQuizDate(
                userId, request.questionId(), today);
        if (alreadyAnswered) {
            throw new RuntimeException("You have already answered this question today");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findByIdWithAnswers(request.questionId());
        if (question == null) {
            throw new QuizNotFoundException("Question not found");
        }

        Answer userAnswer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!userAnswer.getQuestion().getId().equals(question.getId())) {
            throw new RuntimeException("Answer does not belong to the specified question");
        }

        Answer correctAnswer = answerRepository.findCorrectAnswerByQuestionId(request.questionId())
                .orElseThrow(() -> new RuntimeException("Correct answer not found"));

        boolean isCorrect = userAnswer.getIsCorrect();
        int creditsEarned = isCorrect ? 1 : 0;

        if (isCorrect) {
            user.setCredits(user.getCredits() + creditsEarned);
        }

        QuizLog quizLog = QuizLog.builder()
                .user(user)
                .question(question)
                .answer(userAnswer)
                .quizDate(today)
                .isCorrect(isCorrect)
                .creditsEarned(creditsEarned)
                .build();

        quizLogRepository.save(quizLog);

        // Check for perfect score bonus efficiently
        Long totalAnswersToday = todayQuizCount + 1;
        if (totalAnswersToday >= DAILY_QUIZ_LIMIT) {
            Long correctAnswersToday = quizLogRepository.countCorrectAnswersByUserAndDate(userId, today);
            if (correctAnswersToday >= DAILY_QUIZ_LIMIT) {
                user.setCredits(user.getCredits() + PERFECT_SCORE_CREDITS);
                log.info("User {} earned {} credits for perfect daily quiz score", userId, PERFECT_SCORE_CREDITS);
            }
        }

        userRepository.save(user);

        return quizMapper.toQuizResultResponse(
                isCorrect,
                creditsEarned,
                user.getCredits(),
                correctAnswer.getAnswerText()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyQuizStatusResponse getDailyQuizStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> summary = quizLogRepository.getDailyQuizSummary(userId, today);

        Number totalQuizzesNum = (Number) summary.get("totalQuizzes");
        Number totalCreditsNum = (Number) summary.get("totalCredits");

        Long completedQuizzes = totalQuizzesNum != null ? totalQuizzesNum.longValue() : 0L;
        Integer creditsEarnedToday = totalCreditsNum != null ? totalCreditsNum.intValue() : 0;

        return quizMapper.toDailyQuizStatusResponse(
                completedQuizzes,
                DAILY_QUIZ_LIMIT,
                creditsEarnedToday
        );
    }
}