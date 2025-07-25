package com.exe201.project.service;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.response.FeatureResponse;

import java.util.List;

public interface FeatureService {
    FeatureResponse createFeature(FeatureRequest request);
    FeatureResponse updateFeature(Long id, FeatureRequest request);
    void deleteFeature(Long id);
    FeatureResponse getFeatureById(Long id);
    FeatureResponse getFeatureByKey(String featureKey);
    List<FeatureResponse> getAllFeatures();
    List<FeatureResponse> getActiveFeatures();
    List<FeatureResponse> searchFeatures(String keyword);
}
