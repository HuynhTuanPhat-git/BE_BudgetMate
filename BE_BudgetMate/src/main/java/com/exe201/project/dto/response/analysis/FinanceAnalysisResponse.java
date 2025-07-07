package com.exe201.project.dto.response.analysis;

import com.exe201.project.enums.AnalysisType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinanceAnalysisResponse {
    AnalysisType type;
    Double currentLimit;
    Double income;
    Double expense;
    Double currentExpense;
    List<DeptAnalysisResponse> depts;
    List<TransactionAnalysisResponse> transactions;
} 