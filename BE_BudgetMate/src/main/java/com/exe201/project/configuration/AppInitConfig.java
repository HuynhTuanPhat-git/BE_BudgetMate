package com.exe201.project.configuration;

import com.exe201.project.entity.Role;
import com.exe201.project.entity.User;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.repository.RoleRepository;
import com.exe201.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class AppInitConfig {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init() {
        return args -> {
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
        };
    }
}
