package com.exe201.project.service.impl;

import com.exe201.project.dto.request.feature.CreatePurchasableFeatureRequest;
import com.exe201.project.dto.request.feature.UpdatePurchasableFeatureRequest;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.purchasable_feature.PurchasableFeatureResponse;
import com.exe201.project.entity.Feature;
import com.exe201.project.entity.PurchasableFeature;
import com.exe201.project.entity.Subscription;
import com.exe201.project.exception.BadRequestException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.PurchasableFeatureMapper;
import com.exe201.project.repository.FeatureRepository;
import com.exe201.project.repository.PurchasableFeatureRepository;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.service.MembershipPlanService;
import com.exe201.project.service.PurchasableFeatureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchasableFeatureServiceImpl implements PurchasableFeatureService {

    private final PurchasableFeatureRepository purchasableFeatureRepository;
    private final FeatureRepository featureRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanService membershipPlanService;
    private final PurchasableFeatureMapper purchasableFeatureMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PurchasableFeatureResponse createPurchasableFeature(CreatePurchasableFeatureRequest request) {
        Feature feature = featureRepository.findById(request.featureId())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + request.featureId()));

        PurchasableFeature purchasableFeature = buildPurchasableFeature(feature, request);
        PurchasableFeature saved = purchasableFeatureRepository.save(purchasableFeature);

        log.info("Created purchasable feature with id: {}", saved.getId());
        return purchasableFeatureMapper.toPurchasableFeatureResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchasableFeatureResponse getPurchasableFeature(UUID id) {
        PurchasableFeature purchasableFeature = purchasableFeatureRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active purchasable feature not found with id: " + id));

        return purchasableFeatureMapper.toPurchasableFeatureResponse(purchasableFeature);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PurchasableFeatureResponse> getPurchasableFeatures(Pageable pageable) {
        Page<PurchasableFeature> page = purchasableFeatureRepository.findByIsActiveTrue(pageable);
        Page<PurchasableFeatureResponse> responsePage = page.map(purchasableFeatureMapper::toPurchasableFeatureResponse);

        return new PagedResponse<>(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasableFeatureResponse> getFeatures() {
        List<PurchasableFeature> activeFeatures = purchasableFeatureRepository.findByIsActiveTrue();

        return activeFeatures.stream()
                .map(purchasableFeatureMapper::toPurchasableFeatureResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchasableFeatureResponse> getAvailableFeaturesForUser(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId, LocalDate.now());
        if (activeSubscription.isEmpty()) {
            throw new BadRequestException("User does not have an active subscription");
        }

        String currentPlanName = activeSubscription.get().getMembershipPlan().getName();
        List<PurchasableFeature> purchasableFeatures = purchasableFeatureRepository.findAvailableForMembershipPlan(currentPlanName);

        return purchasableFeatures.stream()
                .filter(feature -> {
                    String featureKey = feature.getFeature().getFeatureKey();

                    if ("Premium".equals(currentPlanName)) {
                        return false;
                    }

                    Integer membershipLimit = membershipPlanService.getFeatureLimit(
                            activeSubscription.get().getMembershipPlan().getId(), featureKey);

                    return membershipLimit != null;
                })
                .map(purchasableFeatureMapper::toPurchasableFeatureResponse)
                .toList();
    }

    @Override
    public PurchasableFeatureResponse updatePurchasableFeature(UUID id, UpdatePurchasableFeatureRequest request) {
        PurchasableFeature purchasableFeature = purchasableFeatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchasable feature not found with id: " + id));

        updatePurchasableFeatureFields(purchasableFeature, request);
        PurchasableFeature updated = purchasableFeatureRepository.save(purchasableFeature);

        log.info("Updated purchasable feature with id: {} - Updated fields: {}", updated.getId(), getUpdatedFields(request));
        return purchasableFeatureMapper.toPurchasableFeatureResponse(updated);
    }

    @Override
    public void deletePurchasableFeature(UUID id) {
        PurchasableFeature purchasableFeature = purchasableFeatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchasable feature not found with id: " + id));

        purchasableFeature.setIsActive(false);
        purchasableFeatureRepository.save(purchasableFeature);
        log.info("Soft deleted purchasable feature with id: {}", id);
    }

    private PurchasableFeature buildPurchasableFeature(Feature feature, CreatePurchasableFeatureRequest request) {
        PurchasableFeature purchasableFeature = new PurchasableFeature();
        purchasableFeature.setFeature(feature);
        purchasableFeature.setCreditPrice(request.creditPrice());
        purchasableFeature.setUsageLimit(request.usageLimit());
        purchasableFeature.setDescription(request.description());
        purchasableFeature.setIsActive(true);

        try {
            String targetPlansJson = objectMapper.writeValueAsString(request.targetMembershipPlans());
            purchasableFeature.setTargetMembershipPlans(targetPlansJson);
        } catch (JsonProcessingException e) {
            log.error("Error converting target membership plans to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to process target membership plans");
        }

        return purchasableFeature;
    }

    private void updatePurchasableFeatureFields(PurchasableFeature purchasableFeature, UpdatePurchasableFeatureRequest request) {
        if (request.featureId() != null) {
            Feature feature = featureRepository.findById(request.featureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + request.featureId()));
            purchasableFeature.setFeature(feature);
        }

        if (request.creditPrice() != null) {
            purchasableFeature.setCreditPrice(request.creditPrice());
        }

        if (request.usageLimit() != null) {
            purchasableFeature.setUsageLimit(request.usageLimit());
        }

        if (request.description() != null) {
            purchasableFeature.setDescription(request.description());
        }

        if (request.targetMembershipPlans() != null && !request.targetMembershipPlans().isEmpty()) {
            try {
                String targetPlansJson = objectMapper.writeValueAsString(request.targetMembershipPlans());
                purchasableFeature.setTargetMembershipPlans(targetPlansJson);
            } catch (JsonProcessingException e) {
                log.error("Error converting target membership plans to JSON: {}", e.getMessage());
                throw new RuntimeException("Failed to process target membership plans");
            }
        }
    }

    private String getUpdatedFields(UpdatePurchasableFeatureRequest request) {
        List<String> updatedFields = new java.util.ArrayList<>();

        if (request.featureId() != null) updatedFields.add("featureId");
        if (request.creditPrice() != null) updatedFields.add("creditPrice");
        if (request.usageLimit() != null) updatedFields.add("usageLimit");
        if (request.description() != null) updatedFields.add("description");
        if (request.targetMembershipPlans() != null && !request.targetMembershipPlans().isEmpty()) {
            updatedFields.add("targetMembershipPlans");
        }

        return String.join(", ", updatedFields);
    }
}