package com.exe201.project.dto.response.analysis;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionAnalysisResponse {
    Double totalExpense;
    Double totalIncome;
    String category;
} 