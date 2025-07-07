package com.exe201.project.service;

import com.exe201.project.dto.response.analysis.FinanceAnalysisResponse;
import com.exe201.project.enums.AnalysisType;

public interface AnalysisService {
    FinanceAnalysisResponse getProfileAnalysis(Long userId, AnalysisType type);
} 