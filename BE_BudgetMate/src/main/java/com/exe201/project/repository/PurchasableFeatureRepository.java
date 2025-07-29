package com.exe201.project.repository;

import com.exe201.project.entity.PurchasableFeature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchasableFeatureRepository extends JpaRepository<PurchasableFeature, UUID> {
    Page<PurchasableFeature> findByIsActiveTrue(Pageable pageable);
    List<PurchasableFeature> findByIsActiveTrue();
    Optional<PurchasableFeature> findByIdAndIsActiveTrue(UUID id);
    @Query("SELECT pf " +
            "FROM PurchasableFeature pf " +
            "WHERE pf.isActive = true " +
            "AND pf.targetMembershipPlans LIKE %:planName%")
    List<PurchasableFeature> findAvailableForMembershipPlan(@Param("planName") String planName);
}
