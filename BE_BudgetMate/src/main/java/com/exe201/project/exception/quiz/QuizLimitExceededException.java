package com.exe201.project.exception.quiz;

public class QuizLimitExceededException extends RuntimeException {
    public QuizLimitExceededException(String message) {
        super(message);
    }
}
