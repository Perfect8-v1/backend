package com.perfect8.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Controller for User Management
 * 
 * Provides endpoints for administrators to manage users.
 * In v2.0, this will integrate with a proper User service via Feign.
 * 
 * @version 1.0-plain
 */
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class UserAdminController {

    /**
     * GET /api/admin/users
     * List all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {

        try {
            // TODO: Replace with Feign call to user-service in v2.0
            List<Map<String, Object>> mockUsers = List.of(
                    Map.of(
                            "id", 1,
                            "username", "admin",
                            "email", "admin@perfect8.com",
                            "role", "ADMIN",
                            "status", "ACTIVE",
                            "createdAt", "2024-01-15T10:30:00",
                            "lastLogin", "2025-12-10T06:45:00"
                    ),
                    Map.of(
                            "id", 2,
                            "username", "magnus",
                            "email", "magnus@perfect8.com",
                            "role", "ADMIN",
                            "status", "ACTIVE",
                            "createdAt", "2024-02-20T14:15:00",
                            "lastLogin", "2025-12-09T22:30:00"
                    ),
                    Map.of(
                            "id", 3,
                            "username", "testuser",
                            "email", "test@example.com",
                            "role", "USER",
                            "status", "ACTIVE",
                            "createdAt", "2024-06-10T09:00:00",
                            "lastLogin", "2025-12-08T15:20:00"
                    )
            );

            Map<String, Object> data = new HashMap<>();
            data.put("content", mockUsers);
            data.put("page", Map.of(
                    "number", page,
                    "size", size,
                    "totalElements", 3,
                    "totalPages", 1
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "Users retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return errorResponse("USERS_LIST_ERROR", "Failed to retrieve users: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/users/{id}
     * Get specific user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            // TODO: Replace with Feign call in v2.0
            if (id == 1) {
                Map<String, Object> user = Map.of(
                        "id", 1,
                        "username", "admin",
                        "email", "admin@perfect8.com",
                        "role", "ADMIN",
                        "status", "ACTIVE",
                        "createdAt", "2024-01-15T10:30:00",
                        "lastLogin", "2025-12-10T06:45:00",
                        "profile", Map.of(
                                "firstName", "Admin",
                                "lastName", "User",
                                "phone", "+46 70 123 4567"
                        )
                );

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", user);
                response.put("message", "User retrieved successfully");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", Map.of(
                                "code", "USER_NOT_FOUND",
                                "message", "User with ID " + id + " not found"
                        )
                ));
            }

        } catch (Exception e) {
            return errorResponse("USER_GET_ERROR", "Failed to retrieve user: " + e.getMessage());
        }
    }

    /**
     * PUT /api/admin/users/{id}
     * Update user (status, role, profile)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        try {
            // TODO: Replace with Feign call in v2.0
            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("id", id);
            updatedUser.put("username", "admin");
            updatedUser.put("email", updates.getOrDefault("email", "admin@perfect8.com"));
            updatedUser.put("role", updates.getOrDefault("role", "ADMIN"));
            updatedUser.put("status", updates.getOrDefault("status", "ACTIVE"));
            updatedUser.put("updatedAt", LocalDateTime.now().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedUser);
            response.put("message", "User updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return errorResponse("USER_UPDATE_ERROR", "Failed to update user: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/admin/users/{id}
     * Soft delete (deactivate) user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // TODO: Replace with Feign call in v2.0
            // Soft delete - set status to INACTIVE
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "id", id,
                    "status", "INACTIVE",
                    "deletedAt", LocalDateTime.now().toString()
            ));
            response.put("message", "User deactivated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return errorResponse("USER_DELETE_ERROR", "Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * POST /api/admin/users/{id}/toggle-status
     * Toggle user active/inactive status
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            // TODO: Replace with Feign call in v2.0
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                    "id", id,
                    "previousStatus", "ACTIVE",
                    "newStatus", "INACTIVE",
                    "toggledAt", LocalDateTime.now().toString()
            ));
            response.put("message", "User status toggled successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return errorResponse("USER_TOGGLE_ERROR", "Failed to toggle user status: " + e.getMessage());
        }
    }

    /**
     * Helper method for error responses
     */
    private ResponseEntity<?> errorResponse(String code, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", Map.of(
                "code", code,
                "message", message
        ));
        return ResponseEntity.status(500).body(errorResponse);
    }
}
