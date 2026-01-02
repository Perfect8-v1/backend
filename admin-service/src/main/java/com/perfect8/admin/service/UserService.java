package com.perfect8.admin.service;

import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== CRUD Operations ====================

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Get all active users
     */
    public List<User> findAllActive() {
        return userRepository.findAllActive();
    }

    /**
     * Get users by role
     */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create new user
     */
    public User createUser(String email, String password, String firstName, 
                          String lastName, Set<Role> roles) {
        log.info("Creating new user: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles != null ? roles : new HashSet<>(Set.of(Role.USER)))
                .isActive(true)
                .isEmailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * Update user
     */
    public User updateUser(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());

        return updatedUser;
    }

    /**
     * Delete user (soft delete - deactivate)
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
    }

    /**
     * Hard delete user
     */
    public void hardDeleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("User permanently deleted: {}", userId);
    }

    // ==================== Role Management ====================

    /**
     * Add role to user
     */
    public User addRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);
        log.info("Role {} added to user: {}", role, user.getEmail());

        return updatedUser;
    }

    /**
     * Remove role from user
     */
    public User removeRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.getRoles().remove(role);
        User updatedUser = userRepository.save(user);
        log.info("Role {} removed from user: {}", role, user.getEmail());

        return updatedUser;
    }

    /**
     * Set roles for user (replace all)
     */
    public User setRoles(Long userId, Set<Role> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        log.info("Roles updated for user: {}", user.getEmail());

        return updatedUser;
    }

    // ==================== Account Status ====================

    /**
     * Activate user
     */
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setActive(true);
        User updatedUser = userRepository.save(user);
        log.info("User activated: {}", user.getEmail());

        return updatedUser;
    }

    /**
     * Deactivate user
     */
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setActive(false);
        User updatedUser = userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());

        return updatedUser;
    }

    /**
     * Unlock user account
     */
    public User unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        User updatedUser = userRepository.save(user);
        log.info("Account unlocked: {}", user.getEmail());

        return updatedUser;
    }

    // ==================== Password Management ====================

    /**
     * Change password
     */
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        log.info("Password reset token generated for: {}", email);
        return token;
    }

    /**
     * Reset password with token
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);

        if (userOpt.isEmpty()) {
            log.warn("Invalid reset token: {}", token);
            return false;
        }

        User user = userOpt.get();

        if (user.getResetPasswordTokenExpiry() == null || 
            user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired reset token for user: {}", user.getEmail());
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for: {}", user.getEmail());
        return true;
    }

    // ==================== Email Verification ====================

    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);

        if (userOpt.isEmpty()) {
            log.warn("Invalid email verification token: {}", token);
            return false;
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified for: {}", user.getEmail());
        return true;
    }

    // ==================== Statistics ====================

    /**
     * Count users by role
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /**
     * Get total user count
     */
    public long getTotalCount() {
        return userRepository.count();
    }

    /**
     * Get new users since date
     */
    public List<User> getNewUsersSince(LocalDateTime since) {
        return userRepository.findUsersCreatedSince(since);
    }
}
