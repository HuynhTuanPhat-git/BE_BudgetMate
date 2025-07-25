package com.exe201.project.service.impl;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.response.FeatureResponse;
import com.exe201.project.entity.Feature;
import com.exe201.project.exception.ResourceAlreadyExistException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.FeatureMapper;
import com.exe201.project.repository.FeatureRepository;
import com.exe201.project.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FeatureServiceImpl implements FeatureService {
    
    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

    @Override
    public FeatureResponse createFeature(FeatureRequest request) {
        // Check if feature with same name or key already exists
        if (featureRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistException("Feature with name '" + request.name() + "' already exists");
        }
        
        if (featureRepository.existsByFeatureKey(request.featureKey())) {
            throw new ResourceAlreadyExistException("Feature with key '" + request.featureKey() + "' already exists");
        }

        Feature feature = new Feature();
        feature.setName(request.name());
        feature.setDescription(request.description());
        feature.setFeatureKey(request.featureKey().toUpperCase());
        feature.setIsActive(request.isActive() != null ? request.isActive() : true);

        Feature savedFeature = featureRepository.save(feature);
        return featureMapper.toFeatureResponse(savedFeature);
    }

    @Override
    public FeatureResponse updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        // Check if new name or key conflicts with existing features (excluding current one)
        if (!feature.getName().equals(request.name()) && featureRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistException("Feature with name '" + request.name() + "' already exists");
        }
        
        if (!feature.getFeatureKey().equals(request.featureKey().toUpperCase()) && 
            featureRepository.existsByFeatureKey(request.featureKey())) {
            throw new ResourceAlreadyExistException("Feature with key '" + request.featureKey() + "' already exists");
        }

        feature.setName(request.name());
        feature.setDescription(request.description());
        feature.setFeatureKey(request.featureKey().toUpperCase());
        feature.setIsActive(request.isActive() != null ? request.isActive() : feature.getIsActive());

        Feature updatedFeature = featureRepository.save(feature);
        return featureMapper.toFeatureResponse(updatedFeature);
    }

    @Override
    public void deleteFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
        
        featureRepository.delete(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureResponse getFeatureById(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
        
        return featureMapper.toFeatureResponse(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureResponse getFeatureByKey(String featureKey) {
        Feature feature = featureRepository.findByFeatureKey(featureKey.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with key: " + featureKey));
        
        return featureMapper.toFeatureResponse(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(featureMapper::toFeatureResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> getActiveFeatures() {
        return featureRepository.findByIsActiveTrue().stream()
                .map(featureMapper::toFeatureResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> searchFeatures(String keyword) {
        return featureRepository.searchByKeyword(keyword).stream()
                .map(featureMapper::toFeatureResponse)
                .collect(Collectors.toList());
    }
}
