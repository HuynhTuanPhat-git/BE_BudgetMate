package com.exe201.project.configuration;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.request.MembershipFeatureRequest;
import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Role;
import com.exe201.project.entity.User;
import com.exe201.project.enums.DurationType;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.repository.*;
import com.exe201.project.service.FeatureService;
import com.exe201.project.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AppInitConfig {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final FeatureRepository featureRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final FeatureService featureService;
    private final MembershipPlanService membershipPlanService;
    private final MembershipFeatureRepository membershipFeatureRepository;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(30000);   // 30 seconds
        return new RestTemplate(factory);
    }

    @Bean
    ApplicationRunner init() {
        return args -> {
            initializeCategories();
            initializeRoles();
            initializeAdminUser();
            initializeFeaturesIfNeeded();
            initializeMembershipPlansIfNeeded();
        };
    }

    private void initializeCategories() {
        if (categoryRepository.findByName("TRANSACTION REFACTOR").isEmpty()) {
            Category category = new Category();
            category.setName("TRANSACTION REFACTOR");
            category.setColor("#007bff");
            categoryRepository.save(category);
            log.info("Category TRANSACTION REFACTOR initialized.");
        }

        if (categoryRepository.findByName("MEMBERSHIP").isEmpty()) {
            Category category = new Category();
            category.setName("MEMBERSHIP");
            category.setColor("#fca130");
            categoryRepository.save(category);
            log.info("Category MEMBERSHIP initialized.");
        }
    }

    private void initializeRoles() {
        if (roleRepository.findAll().isEmpty()) {
            Role roleAdmin = new Role();
            roleAdmin.setName("ROLE_ADMIN");
            roleRepository.save(roleAdmin);

            Role roleUser = new Role();
            roleUser.setName("ROLE_USER");
            roleRepository.save(roleUser);

            log.info("Roles initialized.");
        }
    }

    private void initializeAdminUser() {
        if (userRepository.findByEmail("budgetmatecrop@gmail.com").isEmpty()) {
            User admin = createAdminUser();
            userRepository.save(admin);
            log.info("Admin account initialized.");
        }
    }

    private User createAdminUser() {
        User admin = new User();
        admin.setEmail("budgetmatecrop@gmail.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setFullName("Admin");
        admin.setStatus(UserStatus.ACTIVE);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN not exist"));
        admin.setRole(adminRole);

        return admin;
    }

    private void initializeFeaturesIfNeeded() {
        if (featureRepository.count() == 0) {
            initializeFeatures();
            log.info("Features initialized.");
        }
    }

    private void initializeMembershipPlansIfNeeded() {
        if (membershipPlanRepository.count() == 0 || membershipFeatureRepository.count() == 0) {
            if (membershipPlanRepository.count() > 0 && membershipFeatureRepository.count() == 0) {
                log.info("Membership features missing, recreating membership plans...");
                membershipPlanRepository.deleteAll();
            }
            initializeMembershipPlans();
            log.info("Membership plans initialized.");
        }
    }

    private void initializeFeatures() {
        List<FeatureRequest> features = createFeatureRequests();

        for (FeatureRequest feature : features) {
            try {
                featureService.createFeature(feature);
            } catch (Exception e) {
                log.warn("Feature {} already exists or error occurred: {}", feature.name(), e.getMessage());
            }
        }
    }

    private List<FeatureRequest> createFeatureRequests() {
        return Arrays.asList(
                new FeatureRequest("Create SAVINGS Wallets", "Ability to create savings wallets", "CREATE_SAVINGS_WALLETS", true),
                new FeatureRequest("Create DEPT Wallets", "Ability to create dept wallets", "CREATE_DEPT_WALLETS", true),
                new FeatureRequest("Unlimited Transactions", "No limit on number of transactions", "UNLIMITED_TRANSACTIONS", true),
                new FeatureRequest("Advanced Analytics", "Access to advanced financial analytics and reports", "ADVANCED_ANALYTICS", true),
                new FeatureRequest("Export Data", "Ability to export financial data to CSV/PDF", "EXPORT_DATA", true),
                new FeatureRequest("Priority Support", "Access to priority customer support", "PRIORITY_SUPPORT", true),
                new FeatureRequest("Custom Categories", "Create unlimited custom transaction categories", "CUSTOM_CATEGORIES", true),
                new FeatureRequest("Budget Alerts", "Set up budget alerts and notifications", "BUDGET_ALERTS", true),
                new FeatureRequest("Financial Goals", "Set and track financial goals", "FINANCIAL_GOALS", true),
                new FeatureRequest("Multi Currency", "Support for multiple currencies", "MULTI_CURRENCY", true)
        );
    }

    private void initializeMembershipPlans() {
        try {
            List<MembershipRequest> membershipPlans = createMembershipRequests();

            for (MembershipRequest plan : membershipPlans) {
                try {
                    membershipPlanService.createMembershipPlan(plan);
                    log.info("{} plan created successfully", plan.name());
                } catch (Exception e) {
                    log.warn("{} plan already exists or error: {}", plan.name(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error initializing membership plans: {}", e.getMessage());
        }
    }

    private List<MembershipRequest> createMembershipRequests() {
        return Arrays.asList(
                createBasicPlan(),
                createPlusMonthlyPlan(),
                createPlusYearlyPlan(),
                createPremiumMonthlyPlan(),
                createPremiumYearlyPlan()
        );
    }

    private MembershipRequest createBasicPlan() {
        return new MembershipRequest(
                "Basic",
                "Basic features for personal use - includes 1 wallet of each type (Default, Debt, Savings)",
                0.0,
                0.0, // No expiration
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(1L, 1, true, "Can create 1 savings wallet"),
                        new MembershipFeatureRequest(2L, 1, true, "Can create 1 dept wallet")
                )
        );
    }

    private MembershipRequest createPlusMonthlyPlan() {
        return new MembershipRequest(
                "Plus",
                "Enhanced features for power users",
                29000.0,
                1.0, // 1 month
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(1L, 3, true, "Can create 3 savings wallet"),
                        new MembershipFeatureRequest(2L, 3, true, "Can create 3 dept wallet"),
                        new MembershipFeatureRequest(3L, 1000, true, "Up to 1000 transactions per month"),
                        new MembershipFeatureRequest(7L, 20, true, "Up to 20 custom categories"),
                        new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(9L, 10, true, "Up to 10 financial goals")
                )
        );
    }

    private MembershipRequest createPlusYearlyPlan() {
        return new MembershipRequest(
                "Plus",
                "Enhanced features for power users - Yearly",
                290000.0, // 10 months price for yearly subscription
                12.0, // 12 months
                DurationType.YEARLY,
                Arrays.asList(
                        new MembershipFeatureRequest(1L, 3, true, "Can create 3 savings wallet"),
                        new MembershipFeatureRequest(2L, 3, true, "Can create 3 dept wallet"),
                        new MembershipFeatureRequest(3L, 1000, true, "Up to 1000 transactions per month"),
                        new MembershipFeatureRequest(7L, 20, true, "Up to 20 custom categories"),
                        new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(9L, 10, true, "Up to 10 financial goals")
                )
        );
    }

    private MembershipRequest createPremiumMonthlyPlan() {
        return new MembershipRequest(
                "Premium",
                "Full access to all features",
                49000.0,
                1.0, // 1 month
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(1L, null, true, "Can create unlimited savings wallet"),
                        new MembershipFeatureRequest(2L, null, true, "Can create unlimited dept wallet"),
                        new MembershipFeatureRequest(3L, null, true, "Unlimited transactions"),
                        new MembershipFeatureRequest(4L, null, true, "Full advanced analytics"),
                        new MembershipFeatureRequest(5L, null, true, "Export to all formats"),
                        new MembershipFeatureRequest(6L, null, true, "24/7 priority support"),
                        new MembershipFeatureRequest(7L, null, true, "Unlimited custom categories"),
                        new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(9L, null, true, "Unlimited financial goals"),
                        new MembershipFeatureRequest(10L, null, true, "Multi-currency support")
                )
        );
    }

    private MembershipRequest createPremiumYearlyPlan() {
        return new MembershipRequest(
                "Premium",
                "Full access to all features - Yearly",
                490000.0, // 10 months price for yearly subscription
                12.0, // 12 months
                DurationType.YEARLY,
                Arrays.asList(
                        new MembershipFeatureRequest(1L, null, true, "Can create unlimited savings wallet"),
                        new MembershipFeatureRequest(2L, null, true, "Can create unlimited dept wallet"),
                        new MembershipFeatureRequest(3L, null, true, "Unlimited transactions"),
                        new MembershipFeatureRequest(4L, null, true, "Full advanced analytics"),
                        new MembershipFeatureRequest(5L, null, true, "Export to all formats"),
                        new MembershipFeatureRequest(6L, null, true, "24/7 priority support"),
                        new MembershipFeatureRequest(7L, null, true, "Unlimited custom categories"),
                        new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(9L, null, true, "Unlimited financial goals"),
                        new MembershipFeatureRequest(10L, null, true, "Multi-currency support")
                )
        );
    }
}
