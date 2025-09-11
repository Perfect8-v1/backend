package com.perfect8.shop.enums;

/**
 * User Role Enum - Version 1.0
 * Defines the basic roles in the shop system
 */
public enum Role {

    /**
     * Customer role - can browse, buy products, manage their account
     */
    CUSTOMER("ROLE_CUSTOMER", "Customer", 1),

    /**
     * Admin role - full system access
     */
    ADMIN("ROLE_ADMIN", "Administrator", 99),

    /**
     * Staff role - can manage orders and products
     */
    STAFF("ROLE_STAFF", "Staff Member", 50),

    /**
     * Guest role - unauthenticated users (for future use)
     */
    GUEST("ROLE_GUEST", "Guest", 0);

    // Version 2.0 roles - commented out for future
    /*
    MANAGER("ROLE_MANAGER", "Manager", 75),
    WAREHOUSE("ROLE_WAREHOUSE", "Warehouse Staff", 40),
    SUPPORT("ROLE_SUPPORT", "Customer Support", 30),
    VENDOR("ROLE_VENDOR", "Vendor/Supplier", 20),
    AFFILIATE("ROLE_AFFILIATE", "Affiliate Partner", 10),
    VIP_CUSTOMER("ROLE_VIP_CUSTOMER", "VIP Customer", 5);
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

    /**
     * Check if this role is admin or higher
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this role is staff or higher
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
     * Check if this role is guest
     */
    public boolean isGuest() {
        return this == GUEST;
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
     * Check if this role can access admin panel
     */
    public boolean canAccessAdminPanel() {
        return isStaffOrHigher();
    }

    /**
     * Check if this role can make purchases
     */
    public boolean canMakePurchases() {
        return this == CUSTOMER || isStaffOrHigher();
    }

    /**
     * Get role from authority string
     */
    public static Role fromAuthority(String authority) {
        if (authority == null) {
            return null;
        }

        // Handle both with and without ROLE_ prefix
        String normalizedAuth = authority.toUpperCase();
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
            if (role.displayName.equalsIgnoreCase(displayName)) {
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
            return Role.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try without case sensitivity
            for (Role role : values()) {
                if (role.name().equalsIgnoreCase(name)) {
                    return role;
                }
            }
            return null;
        }
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

    @Override
    public String toString() {
        return this.authority;
    }
}