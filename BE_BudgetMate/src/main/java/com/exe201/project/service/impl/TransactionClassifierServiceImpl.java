package com.exe201.project.service.impl;

import com.exe201.project.dto.response.ai.TransactionPredictionResponse;
import com.exe201.project.dto.response.ai.TransactionPredictionTempResponse;
import com.exe201.project.exception.InternalSeverErrorException;
import com.exe201.project.service.TransactionClassifierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionClassifierServiceImpl implements TransactionClassifierService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    public TransactionPredictionTempResponse predictTransaction(String text) {
        try {
            String url = pythonApiUrl + "/predict";

            // Prepare request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<TransactionPredictionTempResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TransactionPredictionTempResponse.class
            );
            log.info("Prediction response for text: {}", response);


            if (response.getStatusCode() == HttpStatus.OK) {
                TransactionPredictionTempResponse result = response.getBody();
                result.setDescription(text);
                log.info("Prediction data for text: {}", result);
                log.info("Prediction successful for text: {}", text);
                return result;
            } else {
                log.error("Python API returned error status: {}", response.getStatusCode());
                throw new InternalSeverErrorException("Python API returned error: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            log.error("Cannot connect to Python API: {}", e.getMessage());
            throw new InternalSeverErrorException("Cannot connect to Python API service");
        } catch (Exception e) {
            log.error("Error calling Python API: {}", e.getMessage(), e);
            throw new InternalSeverErrorException("Error processing transaction: " + e.getMessage());
        }
    }

    public boolean isServiceHealthy() {
        try {
            String url = pythonApiUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}
