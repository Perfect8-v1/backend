package com.perfect8.common.enums;

/**
 * User Role Enum - Version 1.0
 * Unified roles for all Perfect8 services (shop, blog, admin)
 */
public enum Role {

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
     * Customer role - can browse, buy products, comment on blogs
     */
    CUSTOMER("ROLE_CUSTOMER", "Customer", 10),

    /**
     * Guest role - unauthenticated users
     */
    GUEST("ROLE_GUEST", "Guest", 0);

    // Version 2.0 roles - commented out for future
    /*
    MANAGER("ROLE_MANAGER", "Manager", 75),
    MODERATOR("ROLE_MODERATOR", "Content Moderator", 45),
    WAREHOUSE("ROLE_WAREHOUSE", "Warehouse Staff", 35),
    SUPPORT("ROLE_SUPPORT", "Customer Support", 30),
    VENDOR("ROLE_VENDOR", "Vendor/Supplier", 25),
    VIP_CUSTOMER("ROLE_VIP_CUSTOMER", "VIP Customer", 15),
    AFFILIATE("ROLE_AFFILIATE", "Affiliate Partner", 12);
    */

    private final String authority;
    private final String displayName;
    private final int level;

    Role(String authority, String displayName, int level) {
        this.authority = authority;
        this.displayName = displayName;
        this.level = level;
    }

    /**
     * Get the Spring Security authority string
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Get the display name for UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the authority level (higher = more permissions)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Check if this role has at least the given level
     */
    public boolean hasLevel(int requiredLevel) {
        return this.level >= requiredLevel;
    }

    // ========== General Permission Checks ==========

    /**
     * Check if this role is admin
     */
    public boolean isAdmin() {
        return this == ADMIN;
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

    // ========== Shop Service Permissions ==========

    /**
     * Check if this role is staff or higher (for shop management)
     */
    public boolean isStaffOrHigher() {
        return this == STAFF || this == ADMIN;
    }

    /**
     * Check if this role is customer
     */
    public boolean isCustomer() {
        return this == CUSTOMER;
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
        return this == CUSTOMER || isStaffOrHigher();
    }

    // ========== Blog Service Permissions ==========

    /**
     * Check if this role can write blog posts
     */
    public boolean canWriteBlogPosts() {
        return this == WRITER || this == ADMIN;
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
        return isAuthenticated() && this != GUEST;
    }

    // ========== Admin Panel Access ==========

    /**
     * Check if this role can access admin panel
     */
    public boolean canAccessAdminPanel() {
        return this == ADMIN || this == STAFF || this == WRITER;
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

        // Default to CUSTOMER if not found
        return CUSTOMER;
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

        // Default to CUSTOMER for unknown values
        return CUSTOMER;
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
     * Get default role for new customers
     */
    public static Role getDefaultCustomerRole() {
        return CUSTOMER;
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

    @Override
    public String toString() {
        return this.authority;
    }
}