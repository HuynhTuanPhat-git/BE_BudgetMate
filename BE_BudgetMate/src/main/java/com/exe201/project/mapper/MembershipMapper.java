package com.exe201.project.mapper;

import com.exe201.project.dto.response.MembershipFeatureResponse;
import com.exe201.project.dto.response.MembershipResponse;
import com.exe201.project.entity.MembershipPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MembershipMapper {
    
    private final MembershipFeatureMapper membershipFeatureMapper;
    
    public MembershipResponse toMembershipResponse(MembershipPlan membership) {
        if (membership == null) {
            return null;
        }

        List<MembershipFeatureResponse> features =
                membership.getMembershipFeatures() != null ? 
                membership.getMembershipFeatures().stream()
                        .map(membershipFeatureMapper::toMembershipFeatureResponse)
                        .collect(Collectors.toList()) : List.of();

        return MembershipResponse.builder()
                .id(membership.getId())
                .name(membership.getName())
                .description(membership.getDescription())
                .duration(membership.getDuration())
                .price(membership.getPrice())
                .type(membership.getType())
                .status(membership.getStatus())
                .features(features)
                .build();
    }
}
