package com.exe201.project.repository;

import com.exe201.project.entity.UserPurchasedFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPurchasedFeatureRepository extends JpaRepository<UserPurchasedFeature, UUID> {

    List<UserPurchasedFeature> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT upf FROM UserPurchasedFeature upf " +
            "WHERE upf.user.id = :userId " +
            "AND upf.purchasableFeature.feature.featureKey = :featureKey " +
            "AND upf.isActive = true " +
            "AND upf.remainingUsage > 0")
    Optional<UserPurchasedFeature> findFeatureByUserAndKey(@Param("userId") Long userId,
                                                                 @Param("featureKey") String featureKey);

    @Query("SELECT upf FROM UserPurchasedFeature upf " +
            "WHERE upf.user.id = :userId " +
            "AND upf.purchasableFeature.feature.featureKey = :featureKey " +
            "AND upf.isActive = true")
    List<UserPurchasedFeature> findAllFeaturesByUserAndKey(@Param("userId") Long userId,
                                                                 @Param("featureKey") String featureKey);

    @Query("SELECT COALESCE(SUM(upf.remainingUsage), 0) " +
            "FROM UserPurchasedFeature upf " +
            "WHERE upf.user.id = :userId " +
            "AND upf.purchasableFeature.feature.featureKey = :featureKey " +
            "AND upf.isActive = true")
    Integer getTotalRemainingUsageByUserAndFeature(@Param("userId") Long userId,
                                                   @Param("featureKey") String featureKey);
}
