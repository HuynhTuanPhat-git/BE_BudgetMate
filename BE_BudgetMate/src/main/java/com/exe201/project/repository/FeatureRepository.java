package com.exe201.project.repository;

import com.exe201.project.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    
    Optional<Feature> findByFeatureKey(String featureKey);
    
    Optional<Feature> findByName(String name);
    
    List<Feature> findByIsActiveTrue();
    
    boolean existsByFeatureKey(String featureKey);
    
    boolean existsByName(String name);
    
    @Query("SELECT f FROM Feature f WHERE f.name LIKE %:keyword% OR f.description LIKE %:keyword%")
    List<Feature> searchByKeyword(@Param("keyword") String keyword);
}
