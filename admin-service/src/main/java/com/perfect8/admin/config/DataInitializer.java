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
        // Initiera endast om inga användare finns
        if (userRepository.count() == 0) {
            log.info("Initierar standardanvändare för SOP-branschen...");

            // 1. Super Admin - Magnus (Din profil)
            User magnusAdmin = User.builder()
                    .email("cmb@p8.se")
                    .passwordHash(passwordEncoder.encode("magnus123"))
                    .firstName("Magnus")
                    .lastName("Admin")
                    .roles(new HashSet<>(Set.of(Role.SUPER_ADMIN, Role.ADMIN)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(magnusAdmin);
            log.info("Skapat super admin (Magnus): {}", magnusAdmin.getEmail());

            // 2. Super Admin - Jonathan (Kollega)
            User jonathanAdmin = User.builder()
                    .email("jonathan@p8.se")
                    .passwordHash(passwordEncoder.encode("jonathan123"))
                    .firstName("Jonathan")
                    .lastName("Admin")
                    .roles(new HashSet<>(Set.of(Role.SUPER_ADMIN, Role.ADMIN)))
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(jonathanAdmin);
            log.info("Skapat super admin (Jonathan): {}", jonathanAdmin.getEmail());

            // 3. Shop Manager
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
            log.info("Skapat shop manager: {}", shopManager.getEmail());

            // 4. Testkund
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
            log.info("Skapat testkund: {}", customer.getEmail());

            log.info("Standardanvändare har initierats framgångsrikt!");
        } else {
            log.info("Användare finns redan, hoppar över initiering.");
        }
    }
}