package com.exe201.project.configuration;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.request.MembershipFeatureRequest;
import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Role;
import com.exe201.project.entity.User;
import com.exe201.project.enums.DurationType;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.repository.CategoryRepository;
import com.exe201.project.repository.FeatureRepository;
import com.exe201.project.repository.MembershipPlanRepository;
import com.exe201.project.repository.RoleRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.FeatureService;
import com.exe201.project.service.MembershipPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class AppInitConfig {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private MembershipPlanService membershipPlanService;

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
            if(categoryRepository.findByName("TRANSACTION REFACTOR").isEmpty()) {
                Category category = new Category();
                category.setName("TRANSACTION REFACTOR");
                category.setColor("#007bff");
                categoryRepository.save(category);
                log.info("Category TRANSACTION REFACTOR initialized.");
            }
            if(categoryRepository.findByName("MEMBERSHIP").isEmpty()) {
                Category category = new Category();
                category.setName("MEMBERSHIP");
                category.setColor("#fca130");
                categoryRepository.save(category);
                log.info("Category MEMBERSHIP initialized.");
            }

            if(roleRepository.findAll().isEmpty()){
                Role roleAdmin = new Role();
                roleAdmin.setName("ROLE_ADMIN");
                roleRepository.save(roleAdmin);
                Role roleUser = new Role();
                roleUser.setName("ROLE_USER");
                roleRepository.save(roleUser);
                log.info("Roles initialized.");
            }
            if (userRepository.findByEmail("budgetmatecrop@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("budgetmatecrop@gmail.com");
                admin.setPassword(passwordEncoder.encode("123456")); // thay đổi mật khẩu theo nhu cầu
                admin.setFullName("Admin");
                admin.setStatus(UserStatus.ACTIVE);

                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN not exist"));
                admin.setRole(adminRole);

                userRepository.save(admin);
                log.info("Admin account initialized.");
            }

            // Initialize Features
            if(featureRepository.count() == 0) {
                initializeFeatures();
                log.info("Features initialized.");
            }

            // Initialize Membership Plans
            if(membershipPlanRepository.count() == 0) {
                initializeMembershipPlans();
                log.info("Membership plans initialized.");
            }
        };
    }

    private void initializeFeatures() {
        List<FeatureRequest> features = Arrays.asList(
            new FeatureRequest("Create Wallet", "Basic ability to create a wallet", "CREATE_WALLET", true),
            new FeatureRequest("Create Multiple Wallets", "Ability to create multiple wallets (savings, debt, etc.)", "CREATE_MULTIPLE_WALLETS", true),
            new FeatureRequest("Unlimited Transactions", "No limit on number of transactions", "UNLIMITED_TRANSACTIONS", true),
            new FeatureRequest("Advanced Analytics", "Access to advanced financial analytics and reports", "ADVANCED_ANALYTICS", true),
            new FeatureRequest("Export Data", "Ability to export financial data to CSV/PDF", "EXPORT_DATA", true),
            new FeatureRequest("Priority Support", "Access to priority customer support", "PRIORITY_SUPPORT", true),
            new FeatureRequest("Custom Categories", "Create unlimited custom transaction categories", "CUSTOM_CATEGORIES", true),
            new FeatureRequest("Budget Alerts", "Set up budget alerts and notifications", "BUDGET_ALERTS", true),
            new FeatureRequest("Financial Goals", "Set and track financial goals", "FINANCIAL_GOALS", true),
            new FeatureRequest("Multi Currency", "Support for multiple currencies", "MULTI_CURRENCY", true)
        );

        for (FeatureRequest feature : features) {
            try {
                featureService.createFeature(feature);
            } catch (Exception e) {
                log.warn("Feature {} already exists or error occurred: {}", feature.name(), e.getMessage());
            }
        }
    }

    private void initializeMembershipPlans() {
        try {
            // Free Plan
            MembershipRequest freePlan = new MembershipRequest(
                "Basic",
                "Basic features for personal use - includes 1 wallet of each type (Default, Debt, Savings)",
                0.0,
                0.0, // No expiration
                DurationType.MONTHLY,
                Arrays.asList(
                    new MembershipFeatureRequest(1L, 3, true, "Can create 3 wallets (1 Default, 1 Debt, 1 Savings)"),
                    new MembershipFeatureRequest(2L, 3, true, "Can create different wallet types")
                )
            );

            // Plus Month Plan
            MembershipRequest plusMonthPlan = new MembershipRequest(
                    "Plus",
                    "Enhanced features for power users",
                    29000.0,
                    1.0, // 1 month
                    DurationType.MONTHLY,
                    Arrays.asList(
                            new MembershipFeatureRequest(1L, 5, true, "Can create up to 5 wallets"),
                            new MembershipFeatureRequest(2L, 5, true, "Can create multiple wallet types"),
                            new MembershipFeatureRequest(3L, 1000, true, "Up to 1000 transactions per month"),
                            new MembershipFeatureRequest(7L, 20, true, "Up to 20 custom categories"),
                            new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                            new MembershipFeatureRequest(9L, 10, true, "Up to 10 financial goals")
                    )
            );

            // Plus Month Plan
            MembershipRequest plusYearPlan = new MembershipRequest(
                    "Plus",
                    "Enhanced features for power users",
                    29000.0,
                    1.0, // 1 month
                    DurationType.MONTHLY,
                    Arrays.asList(
                            new MembershipFeatureRequest(1L, 5, true, "Can create up to 5 wallets"),
                            new MembershipFeatureRequest(2L, 5, true, "Can create multiple wallet types"),
                            new MembershipFeatureRequest(3L, 1000, true, "Up to 1000 transactions per month"),
                            new MembershipFeatureRequest(7L, 20, true, "Up to 20 custom categories"),
                            new MembershipFeatureRequest(8L, null, true, "Unlimited budget alerts"),
                            new MembershipFeatureRequest(9L, 10, true, "Up to 10 financial goals")
                    )
            );

            // Premium Plan
            MembershipRequest premiumMonthPlan = new MembershipRequest(
                    "Premium",
                    "Full access to all features",
                    49000.0,
                    1.0, // 1 month
                    DurationType.MONTHLY,
                    Arrays.asList(
                            new MembershipFeatureRequest(1L, null, true, "Unlimited wallets"),
                            new MembershipFeatureRequest(2L, null, true, "Unlimited wallet types"),
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

            // Premium Plan
            MembershipRequest premiumYearPlan = new MembershipRequest(
                    "Premium",
                    "Full access to all features",
                    49000.0,
                    1.0, // 1 month
                    DurationType.MONTHLY,
                    Arrays.asList(
                            new MembershipFeatureRequest(1L, null, true, "Unlimited wallets"),
                            new MembershipFeatureRequest(2L, null, true, "Unlimited wallet types"),
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

            // Create membership plans
            try { 
                membershipPlanService.createMembershipPlan(freePlan); 
                log.info("Free plan created successfully");
            } catch (Exception e) { 
                log.warn("Free plan already exists or error: {}", e.getMessage());
            }

            try {
                membershipPlanService.createMembershipPlan(plusMonthPlan);
                log.info("Plus plan created successfully");
            } catch (Exception e) {
                log.warn("Plus plan already exists or error: {}", e.getMessage());
            }

            try {
                membershipPlanService.createMembershipPlan(premiumMonthPlan);
                log.info("Premium plan created successfully");
            } catch (Exception e) {
                log.warn("Premium plan already exists or error: {}", e.getMessage());
            }

            try {
                membershipPlanService.createMembershipPlan(plusYearPlan);
                log.info("Plus plan created successfully");
            } catch (Exception e) {
                log.warn("Plus plan already exists or error: {}", e.getMessage());
            }

            try {
                membershipPlanService.createMembershipPlan(premiumYearPlan);
                log.info("Premium plan created successfully");
            } catch (Exception e) {
                log.warn("Premium plan already exists or error: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error initializing membership plans: {}", e.getMessage());
        }
    }
}
