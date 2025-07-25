package com.exe201.project.dto.response.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionPredictionResponse {

    @JsonProperty("input_text")
    private String inputText;

    private PredictionResult prediction;

    @Data
    public static class PredictionResult {
        private String category;
        private Long amount;

        @JsonProperty("formatted_amount")
        private String formattedAmount;

        private Double confidence;
    }
}
