package com.exe201.project.service;

import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.dto.response.MembershipResponse;

import java.util.List;

public interface MembershipPlanService {
    MembershipResponse createMembershipPlan(MembershipRequest membershipRequest);
    MembershipResponse updateMembershipPlan(Long id, MembershipRequest membershipRequest);
    void deleteMembershipPlan(Long id);
    MembershipResponse getMembershipPlan(Long id);
    List<MembershipResponse> getAllMembershipPlan();
    boolean hasFeatureAccess(Long membershipPlanId, String featureKey);
    Integer getFeatureLimit(Long membershipPlanId, String featureKey);
}
