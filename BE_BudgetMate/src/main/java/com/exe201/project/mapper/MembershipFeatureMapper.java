package com.exe201.project.mapper;

import com.exe201.project.dto.response.MembershipFeatureResponse;
import com.exe201.project.entity.MembershipFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipFeatureMapper {
    
    private final FeatureMapper featureMapper;
    
    public MembershipFeatureResponse toMembershipFeatureResponse(MembershipFeature membershipFeature) {
        if (membershipFeature == null) {
            return null;
        }
        
        return MembershipFeatureResponse.builder()
                .id(membershipFeature.getId())
                .feature(featureMapper.toFeatureResponse(membershipFeature.getFeature()))
                .limitValue(membershipFeature.getLimitValue())
                .isEnabled(membershipFeature.getIsEnabled())
                .description(membershipFeature.getDescription())
                .creditPrice(membershipFeature.getCreditPrice())
                .build();
    }
}
