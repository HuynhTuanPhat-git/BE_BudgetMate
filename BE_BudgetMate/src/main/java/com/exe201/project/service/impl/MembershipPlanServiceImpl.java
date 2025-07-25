package com.exe201.project.service.impl;

import com.exe201.project.dto.request.MembershipFeatureRequest;
import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.dto.response.MembershipResponse;
import com.exe201.project.entity.Feature;
import com.exe201.project.entity.MembershipFeature;
import com.exe201.project.entity.MembershipPlan;
import com.exe201.project.enums.Status;
import com.exe201.project.exception.ResourceAlreadyExistException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.MembershipMapper;
import com.exe201.project.repository.FeatureRepository;
import com.exe201.project.repository.MembershipFeatureRepository;
import com.exe201.project.repository.MembershipPlanRepository;
import com.exe201.project.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final FeatureRepository featureRepository;
    private final MembershipFeatureRepository membershipFeatureRepository;
    private final MembershipMapper membershipMapper;

    @Override
    public MembershipResponse createMembershipPlan(MembershipRequest membershipRequest) {
        // Check if membership plan with same name already exists
        membershipPlanRepository.findByName(membershipRequest.name())
                .ifPresent(membershipPlan -> {
                    throw new ResourceAlreadyExistException("Membership plan with name '" + membershipRequest.name() + "' already exists");
                });

        MembershipPlan membershipPlan = new MembershipPlan();
        membershipPlan.setName(membershipRequest.name());
        membershipPlan.setDescription(membershipRequest.description());
        membershipPlan.setDuration(membershipRequest.duration());
        membershipPlan.setPrice(membershipRequest.price());
        membershipPlan.setType(membershipRequest.type());
        membershipPlan.setStatus(Status.ACTIVE);
        membershipPlan.setMembershipFeatures(new ArrayList<>());
        
        MembershipPlan savedMembership = membershipPlanRepository.save(membershipPlan);
        
        // Add features to membership
        if (membershipRequest.features() != null && !membershipRequest.features().isEmpty()) {
            List<MembershipFeature> membershipFeatures = createMembershipFeatures(savedMembership, membershipRequest.features());
            savedMembership.getMembershipFeatures().addAll(membershipFeatures);
        }
        
        return membershipMapper.toMembershipResponse(savedMembership);
    }

    @Override
    public MembershipResponse updateMembershipPlan(Long id, MembershipRequest membershipRequest) {
        MembershipPlan membershipPlan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));
        
        // Check if new name conflicts with existing plans (excluding current one)
        if (!membershipPlan.getName().equals(membershipRequest.name())) {
            membershipPlanRepository.findByName(membershipRequest.name())
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistException("Membership plan with name '" + membershipRequest.name() + "' already exists");
                    });
        }

        membershipPlan.setName(membershipRequest.name());
        membershipPlan.setDescription(membershipRequest.description());
        membershipPlan.setDuration(membershipRequest.duration());
        membershipPlan.setPrice(membershipRequest.price());
        membershipPlan.setType(membershipRequest.type());
        
        // Update features properly with orphanRemoval
        if (membershipPlan.getMembershipFeatures() != null) {
            membershipPlan.getMembershipFeatures().clear(); // This will trigger orphanRemoval
        } else {
            membershipPlan.setMembershipFeatures(new ArrayList<>());
        }
        
        // Flush to ensure deletion is processed
        membershipPlanRepository.flush();
        
        // Add new features
        if (membershipRequest.features() != null && !membershipRequest.features().isEmpty()) {
            List<MembershipFeature> newFeatures = createMembershipFeaturesForUpdate(membershipPlan, membershipRequest.features());
            membershipPlan.getMembershipFeatures().addAll(newFeatures);
        }
        
        MembershipPlan updatedMembership = membershipPlanRepository.save(membershipPlan);
        return membershipMapper.toMembershipResponse(updatedMembership);
    }

    @Override
    public void deleteMembershipPlan(Long id) {
        MembershipPlan membershipPlan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));
        
        membershipPlanRepository.delete(membershipPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getMembershipPlan(Long id) {
        MembershipPlan membershipPlan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));
        
        return membershipMapper.toMembershipResponse(membershipPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> getAllMembershipPlan() {
        return membershipPlanRepository.findAll().stream()
                .map(membershipMapper::toMembershipResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFeatureAccess(Long membershipPlanId, String featureKey) {
        Optional<MembershipFeature> membershipFeature = membershipFeatureRepository
                .findByMembershipPlanIdAndFeatureKey(membershipPlanId, featureKey.toUpperCase());
        
        return membershipFeature.isPresent() && 
               membershipFeature.get().getIsEnabled() &
               (membershipFeature.get().getLimitValue() == null || membershipFeature.get().getLimitValue() > 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getFeatureLimit(Long membershipPlanId, String featureKey) {
        Optional<MembershipFeature> membershipFeature = membershipFeatureRepository
                .findByMembershipPlanIdAndFeatureKey(membershipPlanId, featureKey.toUpperCase());
        
        if (membershipFeature.isPresent() && membershipFeature.get().getIsEnabled()) {
            return membershipFeature.get().getLimitValue(); // null means unlimited
        }
        
        return 0; // No access
    }
    
    private List<MembershipFeature> createMembershipFeatures(MembershipPlan membershipPlan, List<MembershipFeatureRequest> featureRequests) {
        return featureRequests.stream()
                .map(featureRequest -> {
                    Feature feature = featureRepository.findById(featureRequest.featureId())
                            .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + featureRequest.featureId()));
                    
                    MembershipFeature membershipFeature = new MembershipFeature();
                    membershipFeature.setMembershipPlan(membershipPlan);
                    membershipFeature.setFeature(feature);
                    membershipFeature.setLimitValue(featureRequest.limitValue());
                    membershipFeature.setIsEnabled(featureRequest.isEnabled() != null ? featureRequest.isEnabled() : true);
                    membershipFeature.setDescription(featureRequest.description());
                    membershipFeature.setCreditPrice(featureRequest.creditPrice());
                    return membershipFeatureRepository.save(membershipFeature);
                })
                .collect(Collectors.toList());
    }
    
    private List<MembershipFeature> createMembershipFeaturesForUpdate(MembershipPlan membershipPlan, List<MembershipFeatureRequest> featureRequests) {
        return featureRequests.stream()
                .map(featureRequest -> {
                    Feature feature = featureRepository.findById(featureRequest.featureId())
                            .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + featureRequest.featureId()));
                    
                    MembershipFeature membershipFeature = new MembershipFeature();
                    membershipFeature.setMembershipPlan(membershipPlan);
                    membershipFeature.setFeature(feature);
                    membershipFeature.setLimitValue(featureRequest.limitValue());
                    membershipFeature.setIsEnabled(featureRequest.isEnabled() != null ? featureRequest.isEnabled() : true);
                    membershipFeature.setDescription(featureRequest.description());
                    membershipFeature.setCreditPrice(featureRequest.creditPrice());
                    return membershipFeature;
                })
                .collect(Collectors.toList());
    }
}
