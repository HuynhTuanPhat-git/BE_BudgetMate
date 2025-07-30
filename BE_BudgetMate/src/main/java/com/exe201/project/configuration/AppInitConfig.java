package com.exe201.project.configuration;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.request.MembershipFeatureRequest;
import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Feature;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
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
        // Initialize system categories first
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

        // Initialize user categories
        initializeUserCategories();
    }

    private void initializeUserCategories() {
        String[][] categoryData = {
                { "appliances"      , "#5D6D7E" },  // Slate Blue – thiết bị gia dụng: cảm giác bền bỉ, hiện đại
                { "beauty"          , "#D62AD0" },  // Fuchsia – làm đẹp: nữ tính, nổi bật
                { "bills"           , "#EB984E" },  // Pumpkin Orange – hóa đơn: ấm áp, dễ nhận diện
                { "charity"         , "#FFC0CB" },  // Light Pink – từ thiện: nhẹ nhàng, nhân văn
                { "cosmetics"       , "#FF77FF" },  // Hot Pink – mỹ phẩm: tươi trẻ, nổi bật
                { "credit card"     , "#C0392B" },  // Dark Red – thẻ tín dụng: nghiêm túc, cảnh báo chi tiêu
                { "dining out"      , "#E74C3C" },  // Tomato Red – ăn uống ngoài: ấm áp, kích thích khẩu vị
                { "e-wallet"        , "#17A589" },  // Teal – ví điện tử: hiện đại, tin cậy
                { "education"       , "#3498DB" },  // Sky Blue – giáo dục: tươi sáng, truyền cảm hứng
                { "electronics"     , "#1F618D" },  // Majorelle Blue – điện tử: công nghệ, đẳng cấp
                { "entertainment"   , "#8E44AD" },  // Purple – giải trí: sáng tạo, thú vị
                { "fashion"         , "#D35400" },  // Vermilion – thời trang: cá tính, nổi bật
                { "gifts"           , "#F39C12" },  // Amber – quà tặng: ấm áp, thân thiện
                { "groceries"       , "#7D6608" },  // Olive – thực phẩm: tự nhiên, mộc mạc
                { "health"          , "#27AE60" },  // Emerald – sức khỏe: tươi mát, hy vọng
                { "housing"         , "#6E2C00" },  // Chestnut – nhà ở: vững chắc, an toàn
                { "income"          , "#2ECC71" },  // Mint Green – thu nhập: tích cực, tăng trưởng
                { "insurance"       , "#145A32" },  // Forest Green – bảo hiểm: tin cậy, bền vững
                { "investing"       , "#154360" },  // Midnight Blue – đầu tư: sâu sắc, chuyên nghiệp
                { "loan"            , "#A93226" },  // Brick Red – vay vốn: cảnh báo, quan trọng
                { "others"          , "#95A5A6" },  // Gray – khác: trung tính, không nổi bật
                { "restaurant"      , "#AF601A" },  // Rust – nhà hàng: ấm cúng, sang trọng
                { "salary"          , "#1ABC9C" },  // Turquoise – lương: rõ ràng, tin cậy
                { "shopping"        , "#9B59B6" },  // Amethyst – mua sắm: sang trọng, thú vị
                { "sports"          , "#1F8A70" },  // Teal Dark – thể thao: năng động, khỏe khoắn
                { "tax"             , "#6C3483" },  // Plum – thuế: trang trọng, nghiêm túc
                { "transportation"  , "#F4D03F" },  // Sunflower – giao thông: nổi bật, dễ nhận biết
                { "travel"          , "#117864" },  // Deep Teal – du lịch: mát mẻ, khám phá
                { "utilities"       , "#D68910" },  // Sienna – tiện ích: ấm áp, gần gũi
                { "vehicle"         , "#2E86C1" }  // Steel Blue – phương tiện: tin cậy, chắc chắn
        };

        int initializedCount = 0;
        for (String[] data : categoryData) {
            String name = data[0];
            String color = data[1];
            
            if (categoryRepository.findByName(name).isEmpty()) {
                Category category = new Category();
                category.setName(name);
                category.setColor(color);
                categoryRepository.save(category);
                initializedCount++;
            }
        }
        
        if (initializedCount > 0) {
            log.info("{} user categories initialized.", initializedCount);
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
            waitForFeaturesInitialization();
            initializeMembershipPlans();
            log.info("Membership plans initialized.");
        }
    }

    private void waitForFeaturesInitialization() {
        if (featureRepository.count() == 0) {
            initializeFeatures();
            log.info("Features ensured before membership plans.");
        }
    }

    private void initializeFeatures() {
        try {
            List<FeatureRequest> featureRequests = createFeatureRequests();

            for (FeatureRequest featureRequest : featureRequests) {
                try {
                    if (featureRepository.findByFeatureKey(featureRequest.featureKey()).isEmpty()) {
                        featureService.createFeature(featureRequest);
                        log.info("Created feature: {} ({})", featureRequest.name(), featureRequest.featureKey());
                    }
                } catch (Exception e) {
                    log.warn("Failed to create feature {}: {}", featureRequest.featureKey(), e.getMessage());
                }
            }
            log.info("Features initialization completed. Total features: {}", featureRepository.count());
        } catch (Exception e) {
            log.error("Error during features initialization: {}", e.getMessage());
        }
    }

    private void initializeMembershipPlans() {
        try {
            List<MembershipRequest> membershipRequests = createMembershipRequests();

            for (MembershipRequest membershipRequest : membershipRequests) {
                try {
                    membershipPlanService.createMembershipPlan(membershipRequest);
                } catch (Exception e) {
                    log.warn("{} plan already exists or error: {}",
                            membershipRequest.name(),
                            e.getMessage());
                }
            }

            log.info("Membership plans initialization completed. Total plans: {}",
                    membershipPlanRepository.count());
        } catch (Exception e) {
            log.error("Error during membership plans initialization: {}", e.getMessage());
        }
    }

    private List<FeatureRequest> createFeatureRequests() {
        return Arrays.asList(
                new FeatureRequest("Create Savings Wallets", "Ability to create savings wallets", "CREATE_SAVINGS_WALLETS", true),
                new FeatureRequest("Create Debt Wallets", "Ability to create debt wallets", "CREATE_DEPT_WALLETS", true),
                new FeatureRequest("Unlimited Transactions", "Ability to create unlimited transactions", "UNLIMITED_TRANSACTIONS", true),
                new FeatureRequest("Advanced Analytics", "Access to advanced analytics and reports", "ADVANCED_ANALYTICS", true),
                new FeatureRequest("Export Data", "Ability to export data to various formats", "EXPORT_DATA", true),
                new FeatureRequest("Priority Support", "24/7 priority customer support", "PRIORITY_SUPPORT", true),
                new FeatureRequest("Custom Categories", "Ability to create custom transaction categories", "CUSTOM_CATEGORIES", true),
                new FeatureRequest("Budget Alerts", "Real-time budget and spending alerts", "BUDGET_ALERTS", true),
                new FeatureRequest("Financial Goals", "Set and track financial goals", "FINANCIAL_GOALS", true),
                new FeatureRequest("Multi Currency Support", "Support for multiple currencies", "MULTI_CURRENCY", true)
        );
    }

    private List<MembershipRequest> createMembershipRequests() {
        Map<String, Long> featureIds = getFeatureIdMap();

        return Arrays.asList(
                createBasicPlan(featureIds),
                createPlusMonthlyPlan(featureIds),
                createPlusYearlyPlan(featureIds),
                createPremiumMonthlyPlan(featureIds),
                createPremiumYearlyPlan(featureIds)
        );
    }

    private Map<String, Long> getFeatureIdMap() {
        List<Feature> features = featureRepository.findAll();

        if (features.isEmpty()) {
            log.error("No features found in database. Cannot create membership plans.");
            throw new RuntimeException("Features must be initialized before membership plans");
        }

        Map<String, Long> featureIds = features.stream()
                .collect(Collectors.toMap(
                        Feature::getFeatureKey,
                        Feature::getId
                ));

        String[] requiredFeatures = {
                "CREATE_SAVINGS_WALLETS", "CREATE_DEPT_WALLETS", "UNLIMITED_TRANSACTIONS",
                "ADVANCED_ANALYTICS", "EXPORT_DATA", "PRIORITY_SUPPORT",
                "CUSTOM_CATEGORIES", "BUDGET_ALERTS", "FINANCIAL_GOALS", "MULTI_CURRENCY"
        };

        for (String featureKey : requiredFeatures) {
            if (!featureIds.containsKey(featureKey)) {
                log.error("Required feature {} not found in database", featureKey);
                throw new RuntimeException("Missing required feature: " + featureKey);
            }
        }

        log.info("Feature ID mapping validated successfully. Features count: {}", featureIds.size());
        return featureIds;
    }

    private MembershipRequest createBasicPlan(Map<String, Long> featureIds) {
        return new MembershipRequest(
                "Basic",
                "Basic features for personal use - includes 1 wallet of each type (Default, Debt, Savings)",
                0.0,
                0.0,
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(featureIds.get("CREATE_SAVINGS_WALLETS"), 1, true, "Can create 1 savings wallet"),
                        new MembershipFeatureRequest(featureIds.get("CREATE_DEPT_WALLETS"), 1, true, "Can create 1 dept wallet")
                )
        );
    }

    private MembershipRequest createPlusMonthlyPlan(Map<String, Long> featureIds) {
        return new MembershipRequest(
                "Plus",
                "Enhanced features for power users",
                29000.0,
                1.0,
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(featureIds.get("CREATE_SAVINGS_WALLETS"), 3, true, "Can create 3 savings wallet"),
                        new MembershipFeatureRequest(featureIds.get("CREATE_DEPT_WALLETS"), 3, true, "Can create 3 dept wallet"),
                        new MembershipFeatureRequest(featureIds.get("UNLIMITED_TRANSACTIONS"), 1000, true, "Up to 1000 transactions per month"),
                        new MembershipFeatureRequest(featureIds.get("CUSTOM_CATEGORIES"), 20, true, "Up to 20 custom categories"),
                        new MembershipFeatureRequest(featureIds.get("BUDGET_ALERTS"), null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(featureIds.get("FINANCIAL_GOALS"), 10, true, "Up to 10 financial goals")
                )
        );
    }

    private MembershipRequest createPlusYearlyPlan(Map<String, Long> featureIds) {
        return new MembershipRequest(
                "Plus",
                "Enhanced features for power users - Yearly",
                290000.0,
                12.0,
                DurationType.YEARLY,
                Arrays.asList(
                        new MembershipFeatureRequest(featureIds.get("CREATE_SAVINGS_WALLETS"), 3, true, "Can create 3 savings wallet"),
                        new MembershipFeatureRequest(featureIds.get("CREATE_DEPT_WALLETS"), 3, true, "Can create 3 dept wallet"),
                        new MembershipFeatureRequest(featureIds.get("UNLIMITED_TRANSACTIONS"), 1000, true, "Up to 1000 transactions per month"),
                        new MembershipFeatureRequest(featureIds.get("CUSTOM_CATEGORIES"), 20, true, "Up to 20 custom categories"),
                        new MembershipFeatureRequest(featureIds.get("BUDGET_ALERTS"), null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(featureIds.get("FINANCIAL_GOALS"), 10, true, "Up to 10 financial goals")
                )
        );
    }

    private MembershipRequest createPremiumMonthlyPlan(Map<String, Long> featureIds) {
        return new MembershipRequest(
                "Premium",
                "Full access to all features",
                49000.0,
                1.0,
                DurationType.MONTHLY,
                Arrays.asList(
                        new MembershipFeatureRequest(featureIds.get("CREATE_SAVINGS_WALLETS"), null, true, "Can create unlimited savings wallet"),
                        new MembershipFeatureRequest(featureIds.get("CREATE_DEPT_WALLETS"), null, true, "Can create unlimited dept wallet"),
                        new MembershipFeatureRequest(featureIds.get("UNLIMITED_TRANSACTIONS"), null, true, "Unlimited transactions"),
                        new MembershipFeatureRequest(featureIds.get("ADVANCED_ANALYTICS"), null, true, "Full advanced analytics"),
                        new MembershipFeatureRequest(featureIds.get("EXPORT_DATA"), null, true, "Export to all formats"),
                        new MembershipFeatureRequest(featureIds.get("PRIORITY_SUPPORT"), null, true, "24/7 priority support"),
                        new MembershipFeatureRequest(featureIds.get("CUSTOM_CATEGORIES"), null, true, "Unlimited custom categories"),
                        new MembershipFeatureRequest(featureIds.get("BUDGET_ALERTS"), null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(featureIds.get("FINANCIAL_GOALS"), null, true, "Unlimited financial goals"),
                        new MembershipFeatureRequest(featureIds.get("MULTI_CURRENCY"), null, true, "Multi-currency support")
                )
        );
    }

    private MembershipRequest createPremiumYearlyPlan(Map<String, Long> featureIds) {
        return new MembershipRequest(
                "Premium",
                "Full access to all features - Yearly",
                490000.0,
                12.0,
                DurationType.YEARLY,
                Arrays.asList(
                        new MembershipFeatureRequest(featureIds.get("CREATE_SAVINGS_WALLETS"), null, true, "Can create unlimited savings wallet"),
                        new MembershipFeatureRequest(featureIds.get("CREATE_DEPT_WALLETS"), null, true, "Can create unlimited dept wallet"),
                        new MembershipFeatureRequest(featureIds.get("UNLIMITED_TRANSACTIONS"), null, true, "Unlimited transactions"),
                        new MembershipFeatureRequest(featureIds.get("ADVANCED_ANALYTICS"), null, true, "Full advanced analytics"),
                        new MembershipFeatureRequest(featureIds.get("EXPORT_DATA"), null, true, "Export to all formats"),
                        new MembershipFeatureRequest(featureIds.get("PRIORITY_SUPPORT"), null, true, "24/7 priority support"),
                        new MembershipFeatureRequest(featureIds.get("CUSTOM_CATEGORIES"), null, true, "Unlimited custom categories"),
                        new MembershipFeatureRequest(featureIds.get("BUDGET_ALERTS"), null, true, "Unlimited budget alerts"),
                        new MembershipFeatureRequest(featureIds.get("FINANCIAL_GOALS"), null, true, "Unlimited financial goals"),
                        new MembershipFeatureRequest(featureIds.get("MULTI_CURRENCY"), null, true, "Multi-currency support")
                )
        );
    }
}
