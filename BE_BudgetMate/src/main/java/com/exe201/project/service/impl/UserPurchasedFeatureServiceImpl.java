package com.exe201.project.service.impl;

import com.exe201.project.dto.response.user_purchased_feature.UserPurchasedFeatureResponse;
import com.exe201.project.entity.PurchasableFeature;
import com.exe201.project.entity.User;
import com.exe201.project.entity.UserPurchasedFeature;
import com.exe201.project.exception.InsufficientUsageException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.UserPurchasedFeatureMapper;
import com.exe201.project.repository.PurchasableFeatureRepository;
import com.exe201.project.repository.UserPurchasedFeatureRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.UserPurchasedFeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPurchasedFeatureServiceImpl implements UserPurchasedFeatureService {

    private final UserPurchasedFeatureRepository userPurchasedFeatureRepository;
    private final UserRepository userRepository;
    private final PurchasableFeatureRepository purchasableFeatureRepository;
    private final UserPurchasedFeatureMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserPurchasedFeatureResponse> getUserPurchasedFeatures(Long userId) {
        List<UserPurchasedFeature> purchasedFeatures = userPurchasedFeatureRepository
                .findByUserIdAndIsActiveTrue(userId);

        return purchasedFeatures.stream()
                .map(mapper::toUserPurchasedFeatureResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRemainingUsage(Long userId, String featureKey) {
        Integer remainingUsage = getRemainingUsage(userId, featureKey);
        return remainingUsage > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getRemainingUsage(Long userId, String featureKey) {
        return userPurchasedFeatureRepository
                .getTotalRemainingUsageByUserAndFeature(userId, featureKey.toUpperCase());
    }

    @Override
    public void consumeFeatureUsage(Long userId, String featureKey) {
        Optional<UserPurchasedFeature> purchasedFeature = userPurchasedFeatureRepository
                .findFeatureByUserAndKey(userId, featureKey.toUpperCase());

        if (purchasedFeature.isEmpty()) {
            throw new InsufficientUsageException("No remaining usage for feature: " + featureKey);
        }

        UserPurchasedFeature feature = purchasedFeature.get();
        if (feature.getRemainingUsage() <= 0) {
            throw new InsufficientUsageException("No remaining usage for feature: " + featureKey);
        }

        Integer oldUsage = feature.getRemainingUsage();
        feature.setRemainingUsage(oldUsage - 1);
        feature.setLastUsedAt(LocalDateTime.now());

        if (feature.getRemainingUsage() == 0) {
            feature.setIsActive(false);
        }

        userPurchasedFeatureRepository.save(feature);

        log.info("Feature usage consumed for user {} - Feature: {}, Remaining: {}",
                userId, featureKey, feature.getRemainingUsage());
    }

    @Override
    public void addPurchasedFeature(Long userId, String purchasableFeatureId, Integer usageGranted) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PurchasableFeature purchasableFeature = purchasableFeatureRepository.findById(UUID.fromString(purchasableFeatureId))
                .orElseThrow(() -> new ResourceNotFoundException("Purchasable feature not found"));

        Optional<UserPurchasedFeature> existingFeature = userPurchasedFeatureRepository
                .findFeatureByUserAndKey(userId, purchasableFeature.getFeature().getFeatureKey());

        if (existingFeature.isPresent()) {
            UserPurchasedFeature existing = existingFeature.get();
            existing.setRemainingUsage(existing.getRemainingUsage() + usageGranted);
            existing.setIsActive(true);
            userPurchasedFeatureRepository.save(existing);
        } else {
            UserPurchasedFeature newPurchasedFeature = new UserPurchasedFeature();
            newPurchasedFeature.setUser(user);
            newPurchasedFeature.setPurchasableFeature(purchasableFeature);
            newPurchasedFeature.setRemainingUsage(usageGranted);
            newPurchasedFeature.setIsActive(true);

            userPurchasedFeatureRepository.save(newPurchasedFeature);
        }
    }
}
