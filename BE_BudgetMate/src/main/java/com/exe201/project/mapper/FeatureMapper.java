package com.exe201.project.mapper;

import com.exe201.project.dto.response.FeatureResponse;
import com.exe201.project.entity.Feature;
import org.springframework.stereotype.Component;

@Component
public class FeatureMapper {
    
    public FeatureResponse toFeatureResponse(Feature feature) {
        if (feature == null) {
            return null;
        }
        
        return FeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .description(feature.getDescription())
                .featureKey(feature.getFeatureKey())
                .isActive(feature.getIsActive())
                .build();
    }
}
