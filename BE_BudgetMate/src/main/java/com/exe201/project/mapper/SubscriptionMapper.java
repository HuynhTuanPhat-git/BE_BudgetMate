package com.exe201.project.mapper;

import com.exe201.project.dto.response.SubscriptionResponse;
import com.exe201.project.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    
    private final MembershipMapper membershipMapper;
    
    public SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .paymentMethod(subscription.getPaymentMethod())
                .paymentStatus(subscription.getPaymentStatus())
                .membershipPlan(membershipMapper.toMembershipResponse(subscription.getMembershipPlan()))
                .userId(subscription.getUser().getId())
                .userEmail(subscription.getUser().getEmail())
                .build();
    }
}
