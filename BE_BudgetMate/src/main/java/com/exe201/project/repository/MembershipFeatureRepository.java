package com.exe201.project.repository;

import com.exe201.project.entity.MembershipFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipFeatureRepository extends JpaRepository<MembershipFeature, Long> {
    
    List<MembershipFeature> findByMembershipPlanId(Long membershipPlanId);
    
    List<MembershipFeature> findByFeatureId(Long featureId);
    
    Optional<MembershipFeature> findByMembershipPlanIdAndFeatureId(Long membershipPlanId, Long featureId);
    
    @Query("SELECT mf FROM MembershipFeature mf WHERE mf.membershipPlan.id = :membershipPlanId AND mf.isEnabled = true")
    List<MembershipFeature> findEnabledFeaturesByMembershipPlanId(@Param("membershipPlanId") Long membershipPlanId);
    
    @Query("SELECT mf FROM MembershipFeature mf WHERE mf.membershipPlan.id = :membershipPlanId AND mf.feature.featureKey = :featureKey")
    Optional<MembershipFeature> findByMembershipPlanIdAndFeatureKey(@Param("membershipPlanId") Long membershipPlanId, @Param("featureKey") String featureKey);
    
    void deleteByMembershipPlanId(Long membershipPlanId);
}
