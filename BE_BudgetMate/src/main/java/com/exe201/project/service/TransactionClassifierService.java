package com.exe201.project.service;

import com.exe201.project.dto.response.ai.TransactionPredictionResponse;
import com.exe201.project.dto.response.ai.TransactionPredictionTempResponse;

public interface TransactionClassifierService {
    TransactionPredictionTempResponse predictTransaction(String text);
}
