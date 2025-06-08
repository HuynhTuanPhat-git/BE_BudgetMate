package com.exe201.project.service.impl;

import com.exe201.project.dto.request.answer.UpdateAnswerRequest;
import com.exe201.project.dto.request.question.CreateQuestionRequest;
import com.exe201.project.dto.request.question.QuestionSearchRequest;
import com.exe201.project.dto.request.question.UpdateQuestionRequest;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.entity.Answer;
import com.exe201.project.entity.Question;
import com.exe201.project.exception.quiz.QuizNotFoundException;
import com.exe201.project.mapper.QuestionMapper;
import com.exe201.project.repository.AnswerRepository;
import com.exe201.project.repository.QuestionRepository;
import com.exe201.project.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        log.info("Creating new question with text: {}", request.questionText());

        long correctAnswersCount = request.answers().stream()
                .mapToLong(answer -> answer.isCorrect() ? 1 : 0)
                .sum();

        if (correctAnswersCount != 1) {
            throw new RuntimeException("Question must have exactly one correct answer");
        }

        Question question = Question.builder()
                .questionText(request.questionText())
                .isActive(true)
                .build();

        AtomicInteger displayOrder = new AtomicInteger(1);
        List<Answer> answers = request.answers().stream()
                .map(answerRequest -> Answer.builder()
                        .answerText(answerRequest.answerText())
                        .isCorrect(answerRequest.isCorrect())
                        .displayOrder(answerRequest.displayOrder() != null ?
                                answerRequest.displayOrder() : displayOrder.getAndIncrement())
                        .question(question)
                        .build())
                .toList();

        question.setAnswers(answers);
        Question savedQuestion = questionRepository.save(question);

        log.info("Question created successfully with ID: {}", savedQuestion.getId());
        return questionMapper.toQuestionResponse(savedQuestion);
    }

    @Override
    public QuestionResponse updateQuestion(UUID questionId, UpdateQuestionRequest request) {
        log.info("Updating question with ID: {}", questionId);

        Question question = questionRepository.findByIdWithAnswers(questionId);
        if (question == null) {
            throw new QuizNotFoundException("Question not found with ID: " + questionId);
        }

        if (request.questionText() != null) {
            question.setQuestionText(request.questionText());
        }

        if (request.isActive() != null) {
            question.setIsActive(request.isActive());
        }

        if (request.answers() != null) {
            Map<UUID, Answer> existingAnswersMap = question.getAnswers().stream()
                    .collect(Collectors.toMap(Answer::getId, Function.identity()));
            List<Answer> newAnswersCreated = new ArrayList<>();

            AtomicInteger newDisplayOrderAllocator = new AtomicInteger(
                    question.getAnswers().stream()
                            .mapToInt(ans -> ans.getDisplayOrder() != null ? ans.getDisplayOrder() : 0)
                            .max().orElse(0) + 1
            );

            for (UpdateAnswerRequest ansReq : request.answers()) {
                if (ansReq.id() != null) {
                    Answer answerToUpdate = existingAnswersMap.get(ansReq.id());
                    if (answerToUpdate == null) {
                        log.warn("Attempting to update answer with ID {} which does not belong to question {}", ansReq.id(), questionId);
                        // Optionally throw an exception or just skip
                        // throw new IllegalArgumentException("Answer with ID " + ansReq.id() + " not found for question " + questionId);
                        continue;
                    }

                    if (ansReq.answerText() != null) {
                        answerToUpdate.setAnswerText(ansReq.answerText());
                    }
                    if (ansReq.isCorrect() != null) {
                        answerToUpdate.setIsCorrect(ansReq.isCorrect());
                    }
                    if (ansReq.displayOrder() != null) {
                        answerToUpdate.setDisplayOrder(ansReq.displayOrder());
                    }
                    if (ansReq.isActive() != null) {
                        answerToUpdate.setIsActive(ansReq.isActive());
                    }
                } else {
                    if (ansReq.answerText() == null || ansReq.answerText().isBlank()) {
                        throw new IllegalArgumentException("Answer text is required for new answers.");
                    }
                    Answer newAnswer = Answer.builder()
                            .answerText(ansReq.answerText())
                            .isCorrect(ansReq.isCorrect() != null ? ansReq.isCorrect() : false)
                            .displayOrder(ansReq.displayOrder() != null ? ansReq.displayOrder() : newDisplayOrderAllocator.getAndIncrement())
                            .isActive(ansReq.isActive() != null ? ansReq.isActive() : true) // Default new answers to active
                            .question(question)
                            .build();
                    newAnswersCreated.add(newAnswer);
                }
            }
            if (!newAnswersCreated.isEmpty()) {
                question.getAnswers().addAll(newAnswersCreated);
            }
            long activeCorrectCount = question.getAnswers().stream()
                    .filter(ans -> Boolean.TRUE.equals(ans.getIsActive()) && Boolean.TRUE.equals(ans.getIsCorrect()))
                    .count();
            if (activeCorrectCount != 1) {
                throw new RuntimeException("A question must have exactly one active correct answer. After update, found: " + activeCorrectCount + " active correct answers.");
            }
        }
        Question updatedQuestion = questionRepository.save(question);
        log.info("Question updated successfully with ID: {}", updatedQuestion.getId());
        return questionMapper.toQuestionResponse(updatedQuestion);
    }

    @Override
    public void deleteQuestion(UUID questionId) {
        log.info("Soft deleting question with ID: {}", questionId);
        Question question = questionRepository.findByIdWithAnswers(questionId);
        if (question == null) {
            throw new QuizNotFoundException("Question not found with ID: " + questionId);
        }
        question.setIsActive(false);
        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                answer.setIsActive(false);
            }
        }
        questionRepository.save(question);

        log.info("Question and its answers soft deleted successfully with ID: {}", questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(UUID questionId) {
        Question question = questionRepository.findByIdWithAnswers(questionId);
        if (question == null) {
            throw new QuizNotFoundException("Question not found with ID: " + questionId);
        }
        return questionMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getQuestions(QuestionSearchRequest request) {
        log.info("Searching questions with keyword: {}, isActive: {}", request.keyword(), request.isActive());

        Sort sort = Sort.by(Sort.Direction.fromString(request.sortDirection()), request.sortBy());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Question> questionPage = questionRepository.findQuestionsWithFilters(
                request.keyword(), request.isActive(), pageable);

        return questionPage.map(questionMapper::toQuestionResponse);
    }
}