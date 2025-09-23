package com.perfect8.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User Role Enum - Version 1.0
 * Unified roles for all Perfect8 services (shop, blog, admin)
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    /**
     * Super Admin role - highest level access
     */
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "Super Administrator", 100),

    /**
     * Admin role - full system access across all services
     */
    ADMIN("ROLE_ADMIN", "Administrator", 99),

    /**
     * Staff role - can manage shop orders and products
     */
    STAFF("ROLE_STAFF", "Staff Member", 50),

    /**
     * Writer role - can create and edit blog posts
     */
    WRITER("ROLE_WRITER", "Blog Writer", 40),

    /**
     * Reader role - can access premium blog content
     */
    READER("ROLE_READER", "Premium Reader", 20),

    /**
     * User role - standard registered user (customer)
     */
    USER("ROLE_USER", "User", 10),

    /**
     * Customer role - alias for USER, kept for compatibility
     */
    CUSTOMER("ROLE_CUSTOMER", "Customer", 10),

    /**
     * Guest role - unauthenticated users
     */
    GUEST("ROLE_GUEST", "Guest", 0);

    private final String authority;
    private final String displayName;
    private final int level;

    /**
     * Check if this role has at least the given level
     */
    public boolean hasLevel(int requiredLevel) {
        return this.level >= requiredLevel;
    }

    // ========== General Permission Checks ==========

    /**
     * Check if this role is admin (ADMIN or SUPER_ADMIN)
     */
    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    /**
     * Check if this role is super admin
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    /**
     * Check if this role is guest
     */
    public boolean isGuest() {
        return this == GUEST;
    }

    /**
     * Check if this role is authenticated (not guest)
     */
    public boolean isAuthenticated() {
        return this != GUEST;
    }

    /**
     * Check if this role is a regular user (USER or CUSTOMER)
     */
    public boolean isUser() {
        return this == USER || this == CUSTOMER;
    }

    // ========== Shop Service Permissions ==========

    /**
     * Check if this role is staff or higher (for shop management)
     */
    public boolean isStaffOrHigher() {
        return this == STAFF || isAdmin();
    }

    /**
     * Check if this role can manage orders
     */
    public boolean canManageOrders() {
        return isStaffOrHigher();
    }

    /**
     * Check if this role can manage products
     */
    public boolean canManageProducts() {
        return isStaffOrHigher();
    }

    /**
     * Check if this role can manage customers
     */
    public boolean canManageCustomers() {
        return isAdmin();
    }

    /**
     * Check if this role can make purchases
     */
    public boolean canMakePurchases() {
        return isUser() || isStaffOrHigher();
    }

    /**
     * Check if this role can view own orders
     */
    public boolean canViewOwnOrders() {
        return isAuthenticated();
    }

    /**
     * Check if this role can view all orders
     */
    public boolean canViewAllOrders() {
        return isStaffOrHigher();
    }

    // ========== Blog Service Permissions ==========

    /**
     * Check if this role can write blog posts
     */
    public boolean canWriteBlogPosts() {
        return this == WRITER || isAdmin();
    }

    /**
     * Check if this role can moderate comments
     */
    public boolean canModerateComments() {
        return this == WRITER || isStaffOrHigher();
    }

    /**
     * Check if this role can read premium content
     */
    public boolean canReadPremiumContent() {
        return this == READER || this == WRITER || isStaffOrHigher();
    }

    /**
     * Check if this role can comment on blog posts
     */
    public boolean canComment() {
        return isAuthenticated();
    }

    /**
     * Check if this role can delete any blog post
     */
    public boolean canDeleteAnyBlogPost() {
        return isAdmin();
    }

    // ========== Admin Panel Access ==========

    /**
     * Check if this role can access admin panel
     */
    public boolean canAccessAdminPanel() {
        return this == ADMIN || this == SUPER_ADMIN || this == STAFF || this == WRITER;
    }

    /**
     * Check if this role can access shop admin
     */
    public boolean canAccessShopAdmin() {
        return isStaffOrHigher();
    }

    /**
     * Check if this role can access blog admin
     */
    public boolean canAccessBlogAdmin() {
        return canWriteBlogPosts();
    }

    /**
     * Check if this role can access system settings
     */
    public boolean canAccessSystemSettings() {
        return isAdmin();
    }

    // ========== Conversion Methods ==========

    /**
     * Get role from authority string
     */
    public static Role fromAuthority(String authority) {
        if (authority == null) {
            return null;
        }

        // Handle both with and without ROLE_ prefix
        String normalizedAuth = authority.toUpperCase().trim();
        if (!normalizedAuth.startsWith("ROLE_")) {
            normalizedAuth = "ROLE_" + normalizedAuth;
        }

        for (Role role : values()) {
            if (role.authority.equals(normalizedAuth)) {
                return role;
            }
        }

        // Default to USER if not found
        return USER;
    }

    /**
     * Get role from display name
     */
    public static Role fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (Role role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName.trim())) {
                return role;
            }
        }

        return null;
    }

    /**
     * Get role by name (enum name)
     */
    public static Role fromName(String name) {
        if (name == null) {
            return null;
        }

        try {
            return Role.valueOf(name.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Try without case sensitivity
            for (Role role : values()) {
                if (role.name().equalsIgnoreCase(name.trim())) {
                    return role;
                }
            }
            return null;
        }
    }

    /**
     * Universal converter - tries all conversion methods
     */
    public static Role from(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return null;
        }

        // Try by enum name first
        Role role = fromName(roleString);
        if (role != null) return role;

        // Try by authority
        role = fromAuthority(roleString);
        if (role != null) return role;

        // Try by display name
        role = fromDisplayName(roleString);
        if (role != null) return role;

        // Default to USER for unknown values
        return USER;
    }

    /**
     * Check if a string is a valid role
     */
    public static boolean isValidRole(String roleStr) {
        return fromAuthority(roleStr) != null ||
                fromDisplayName(roleStr) != null ||
                fromName(roleStr) != null;
    }

    /**
     * Get default role for new users
     */
    public static Role getDefaultUserRole() {
        return USER;
    }

    /**
     * Get default role for new customers (alias for getDefaultUserRole)
     */
    public static Role getDefaultCustomerRole() {
        return USER;
    }

    /**
     * Get default role for new staff
     */
    public static Role getDefaultStaffRole() {
        return STAFF;
    }

    /**
     * Get default role for new blog writers
     */
    public static Role getDefaultWriterRole() {
        return WRITER;
    }

    /**
     * Get default role for guests
     */
    public static Role getGuestRole() {
        return GUEST;
    }

    @Override
    public String toString() {
        return this.authority;
    }

    /**
     * Check if this role has higher authority than another role
     */
    public boolean hasHigherAuthorityThan(Role otherRole) {
        if (otherRole == null) {
            return true;
        }
        return this.level > otherRole.level;
    }

    /**
     * Check if this role has same or higher authority than another role
     */
    public boolean hasSameOrHigherAuthorityThan(Role otherRole) {
        if (otherRole == null) {
            return true;
        }
        return this.level >= otherRole.level;
    }
}