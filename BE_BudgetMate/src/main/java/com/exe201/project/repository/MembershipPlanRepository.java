package com.exe201.project.repository;

import com.exe201.project.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    Optional<MembershipPlan> findByName(String name);
}
