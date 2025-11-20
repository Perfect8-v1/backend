package com.perfect8.admin.config;

import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            log.info("Initializing default users...");

            // Super Admin
            User superAdmin = User.builder()
                    .email("admin@perfect8.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Admin")
                    .roles(new HashSet<>(Set.of(Role.SUPER_ADMIN, Role.ADMIN)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(superAdmin);
            log.info("Created super admin: {}", superAdmin.getEmail());

            // Shop Manager (Staff)
            User shopManager = User.builder()
                    .email("shop@perfect8.com")
                    .passwordHash(passwordEncoder.encode("shop123"))
                    .firstName("Shop")
                    .lastName("Manager")
                    .roles(new HashSet<>(Set.of(Role.STAFF)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(shopManager);
            log.info("Created shop manager: {}", shopManager.getEmail());

            // Content Editor (Writer)
            User contentEditor = User.builder()
                    .email("writer@perfect8.com")
                    .passwordHash(passwordEncoder.encode("writer123"))
                    .firstName("Content")
                    .lastName("Writer")
                    .roles(new HashSet<>(Set.of(Role.WRITER)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(contentEditor);
            log.info("Created content editor: {}", contentEditor.getEmail());

            // Test Customer
            User customer = User.builder()
                    .email("customer@test.com")
                    .passwordHash(passwordEncoder.encode("customer123"))
                    .firstName("Test")
                    .lastName("Customer")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(customer);
            log.info("Created test customer: {}", customer.getEmail());

            log.info("Default users initialized successfully!");
        } else {
            log.info("Users already exist, skipping initialization.");
        }
    }
}
