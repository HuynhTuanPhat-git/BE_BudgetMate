package com.exe201.project.dto.response.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionPredictionTempResponse {
    private String description;
    private String category;
    private Long amount;
    private Double confidence;
}
