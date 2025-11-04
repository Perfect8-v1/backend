package com.perfect8.admin.service;

import com.perfect8.admin.model.AdminUser;
import com.perfect8.admin.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<AdminUser> findAll() {
        return adminUserRepository.findAll();
    }

    public Optional<AdminUser> findById(Long id) {
        return adminUserRepository.findById(id);
    }

    public AdminUser findByUsername(String username) {
        return adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public Optional<AdminUser> findByEmail(String email) {
        return adminUserRepository.findByEmail(email);
    }

    public AdminUser save(AdminUser adminUser) {
        // Encrypt password if it's being set/changed
        if (adminUser.getPassword() != null && !adminUser.getPassword().startsWith("$2a$")) {
            adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        }
        return adminUserRepository.save(adminUser);
    }

    public void deleteById(Long id) {
        adminUserRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return adminUserRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return adminUserRepository.existsByEmail(email);
    }
}