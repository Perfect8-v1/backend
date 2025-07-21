// blog-service/src/main/java/com/perfect8/blog/config/DataInitializer.java

        package com.perfect8.blog.config;

import com.perfect8.blog.model.Role;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.RoleRepository;
import com.perfect8.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(RoleRepository roleRepository, UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            // Create roles if they don't exist
            if (!roleRepository.existsByName("ADMIN")) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);
            }

            if (!roleRepository.existsByName("WRITER")) {
                Role writerRole = new Role();
                writerRole.setName("WRITER");
                roleRepository.save(writerRole);
            }

            if (!roleRepository.existsByName("READER")) {
                Role readerRole = new Role();
                readerRole.setName("READER");
                roleRepository.save(readerRole);
            }

            // Create default admin user if doesn't exist
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@perfect8.com");
                admin.setPassword(passwordEncoder.encode("admin123"));

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(roleRepository.findByName("ADMIN").orElseThrow());
                adminRoles.add(roleRepository.findByName("WRITER").orElseThrow());
                adminRoles.add(roleRepository.findByName("READER").orElseThrow());
                admin.setRoles(adminRoles);

                userRepository.save(admin);
            }
        };
    }
}