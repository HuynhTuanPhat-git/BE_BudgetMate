package com.exe201.project.mapper;

import com.exe201.project.dto.response.purchasable_feature.PurchasableFeatureResponse;
import com.exe201.project.entity.PurchasableFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchasableFeatureMapper {

    private final ObjectMapper objectMapper;

    public PurchasableFeatureResponse toPurchasableFeatureResponse(PurchasableFeature purchasableFeature) {
        if (purchasableFeature == null) {
            return null;
        }

        List<String> targetPlans = parseTargetMembershipPlans(purchasableFeature.getTargetMembershipPlans());

        return PurchasableFeatureResponse.builder()
                .id(purchasableFeature.getId())
                .featureName(purchasableFeature.getFeature().getName())
                .featureKey(purchasableFeature.getFeature().getFeatureKey())
                .creditPrice(purchasableFeature.getCreditPrice())
                .usageLimit(purchasableFeature.getUsageLimit())
                .isActive(purchasableFeature.getIsActive())
                .targetMembershipPlans(targetPlans)
                .description(purchasableFeature.getDescription())
                .build();
    }

    private List<String> parseTargetMembershipPlans(String targetMembershipPlansJson) {
        if (targetMembershipPlansJson == null || targetMembershipPlansJson.trim().isEmpty()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    targetMembershipPlansJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException e) {
            log.error("Error parsing target membership plans JSON: {}", e.getMessage());
            return List.of();
        }
    }
}
