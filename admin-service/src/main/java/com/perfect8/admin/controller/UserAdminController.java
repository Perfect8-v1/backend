package com.perfect8.admin.controller;

import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin Controller for User Management
 * Reads real data from adminDB.users via UserRepository.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {

    private final UserRepository userRepository;

    /**
     * GET /api/admin/users
     * List all users with pagination and optional filtering.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);

            List<Map<String, Object>> users = userPage.getContent().stream()
                    .filter(u -> {
                        if (status == null) return true;
                        boolean wantActive = "ACTIVE".equalsIgnoreCase(status);
                        return u.isActive() == wantActive;
                    })
                    .filter(u -> {
                        if (role == null) return true;
                        try {
                            Role r = Role.valueOf(role.toUpperCase());
                            return u.getRoles().contains(r);
                        } catch (IllegalArgumentException ex) {
                            return true;
                        }
                    })
                    .map(this::toMap)
                    .collect(Collectors.toList());

            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("number", userPage.getNumber());
            pageInfo.put("size", userPage.getSize());
            pageInfo.put("totalElements", userPage.getTotalElements());
            pageInfo.put("totalPages", userPage.getTotalPages());

            Map<String, Object> data = new HashMap<>();
            data.put("content", users);
            data.put("page", pageInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "Users retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to retrieve users", e);
            return errorResponse("USERS_LIST_ERROR", "Failed to retrieve users: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of(
                                "code", "USER_NOT_FOUND",
                                "message", "User with ID " + userId + " not found"
                        )
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", toMap(userOpt.get()));
            response.put("message", "User retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to retrieve user {}", userId, e);
            return errorResponse("USER_GET_ERROR", "Failed to retrieve user: " + e.getMessage());
        }
    }

    /**
     * PUT /api/admin/users/{userId}
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of("code", "USER_NOT_FOUND", "message", "User not found")
                ));
            }

            User user = userOpt.get();
            if (updates.containsKey("status")) {
                user.setActive("ACTIVE".equalsIgnoreCase((String) updates.get("status")));
            }
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", toMap(user));
            response.put("message", "User updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update user {}", userId, e);
            return errorResponse("USER_UPDATE_ERROR", "Failed to update user: " + e.getMessage());
        }
    }

    /**
     * POST /api/admin/users/{userId}/toggle-status
     */
    @PostMapping("/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of("code", "USER_NOT_FOUND", "message", "User not found")
                ));
            }

            User user = userOpt.get();
            boolean previous = user.isActive();
            user.setActive(!previous);
            userRepository.save(user);

            log.info("Toggled user {} status: {} -> {}", userId, previous, user.isActive());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "userId", userId,
                    "previousStatus", previous ? "ACTIVE" : "INACTIVE",
                    "newStatus", user.isActive() ? "ACTIVE" : "INACTIVE",
                    "toggledAt", LocalDateTime.now().toString()
            ));
            response.put("message", "User status toggled successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to toggle status for user {}", userId, e);
            return errorResponse("USER_TOGGLE_ERROR", "Failed to toggle user status: " + e.getMessage());
        }
    }

    /**
     * POST /api/admin/users/{userId}/toggle-role
     * Togglar mellan ADMIN och USER.
     */
    @PostMapping("/{userId}/toggle-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserRole(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of("code", "USER_NOT_FOUND", "message", "User not found")
                ));
            }

            User user = userOpt.get();
            String previousRole;
            String newRole;

            if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.SUPER_ADMIN)) {
                previousRole = "ADMIN";
                user.getRoles().remove(Role.ADMIN);
                user.getRoles().remove(Role.SUPER_ADMIN);
                user.getRoles().add(Role.USER);
                newRole = "USER";
            } else {
                previousRole = "USER";
                user.getRoles().remove(Role.USER);
                user.getRoles().remove(Role.CUSTOMER);
                user.getRoles().add(Role.ADMIN);
                newRole = "ADMIN";
            }

            userRepository.save(user);
            log.info("Toggled user {} role: {} -> {}", userId, previousRole, newRole);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "userId", userId,
                    "previousRole", previousRole,
                    "newRole", newRole,
                    "toggledAt", LocalDateTime.now().toString()
            ));
            response.put("message", "User role toggled successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to toggle role for user {}", userId, e);
            return errorResponse("USER_ROLE_ERROR", "Failed to toggle user role: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/admin/users/{userId}
     * Soft delete - sätter isActive = false.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of("code", "USER_NOT_FOUND", "message", "User not found")
                ));
            }

            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);

            log.info("Soft-deleted user {}", userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "userId", userId,
                    "status", "INACTIVE",
                    "deactivatedAt", LocalDateTime.now().toString()
            ));
            response.put("message", "User deactivated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to deactivate user {}", userId, e);
            return errorResponse("USER_DELETE_ERROR", "Failed to deactivate user: " + e.getMessage());
        }
    }

    // ========== Helpers ==========

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("email", user.getEmail());
        map.put("firstName", user.getFirstName());
        map.put("lastName", user.getLastName());
        map.put("phone", user.getPhone());
        map.put("roles", user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList()));
        // Primär roll för enkel visning i Django
        map.put("role", user.isAdmin() ? "ADMIN" : "USER");
        map.put("status", user.isActive() ? "ACTIVE" : "INACTIVE");
        map.put("isActive", user.isActive());
        map.put("isEmailVerified", user.isEmailVerified());
        map.put("createdDate", user.getCreatedDate() != null ? user.getCreatedDate().toString() : null);
        map.put("lastLoginDate", user.getLastLoginDate() != null ? user.getLastLoginDate().toString() : null);
        map.put("failedLoginAttempts", user.getFailedLoginAttempts());
        return map;
    }

    private ResponseEntity<?> errorResponse(String code, String message) {
        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", Map.of("code", code, "message", message)
        ));
    }
}
