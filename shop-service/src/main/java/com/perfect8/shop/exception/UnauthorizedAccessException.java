package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    private String userId;
    private String resource;
    private String action;
    private String requiredRole;
    private String currentRole;

    // Default constructor
    public UnauthorizedAccessException() {
        super();
    }

    // Constructor with message
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with access details
    public UnauthorizedAccessException(String userId, String resource, String action) {
        super(formatMessage(userId, resource, action));
        this.userId = userId;
        this.resource = resource;
        this.action = action;
    }

    // Constructor with role details
    public UnauthorizedAccessException(String userId, String resource, String action,
                                       String requiredRole, String currentRole) {
        super(formatMessageWithRoles(userId, resource, action, requiredRole, currentRole));
        this.userId = userId;
        this.resource = resource;
        this.action = action;
        this.requiredRole = requiredRole;
        this.currentRole = currentRole;
    }

    // Static factory methods for common scenarios
    public static UnauthorizedAccessException insufficientRole(String requiredRole, String currentRole) {
        // FIXED: Use the 5-parameter constructor properly
        UnauthorizedAccessException ex = new UnauthorizedAccessException(
                null,  // userId
                null,  // resource
                null,  // action
                requiredRole,
                currentRole
        );
        return ex;
    }

    public static UnauthorizedAccessException accessDenied(String resource) {
        return new UnauthorizedAccessException(
                String.format("Access denied to resource: %s", resource)
        );
    }

    public static UnauthorizedAccessException actionNotAllowed(String action, String resource) {
        return new UnauthorizedAccessException(
                null,
                resource,
                action
        );
    }

    public static UnauthorizedAccessException customerCanOnlyAccessOwnData() {
        return new UnauthorizedAccessException(
                "Customers can only access their own data"
        );
    }

    public static UnauthorizedAccessException adminRequired() {
        return new UnauthorizedAccessException(
                "This operation requires administrator privileges"
        );
    }

    public static UnauthorizedAccessException userNotAuthenticated() {
        return new UnauthorizedAccessException(
                "User must be authenticated to perform this action"
        );
    }

    public static UnauthorizedAccessException tokenExpired() {
        return new UnauthorizedAccessException(
                "Authentication token has expired"
        );
    }

    public static UnauthorizedAccessException invalidToken() {
        return new UnauthorizedAccessException(
                "Invalid authentication token"
        );
    }

    public static UnauthorizedAccessException accountDisabled() {
        return new UnauthorizedAccessException(
                "User account has been disabled"
        );
    }

    public static UnauthorizedAccessException orderAccess(Long orderId, String userId) {
        return new UnauthorizedAccessException(
                userId,
                "Order #" + orderId,
                "access"
        );
    }

    public static UnauthorizedAccessException paymentAccess(Long paymentId, String userId) {
        return new UnauthorizedAccessException(
                userId,
                "Payment #" + paymentId,
                "access"
        );
    }

    public static UnauthorizedAccessException customerDataAccess(Long customerId, String userId) {
        return new UnauthorizedAccessException(
                userId,
                "Customer #" + customerId,
                "access"
        );
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    // Utility methods
    public boolean hasUserId() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasResource() {
        return resource != null && !resource.trim().isEmpty();
    }

    public boolean hasAction() {
        return action != null && !action.trim().isEmpty();
    }

    public boolean hasRoleInfo() {
        return requiredRole != null || currentRole != null;
    }

    public boolean isRoleBasedError() {
        return requiredRole != null && currentRole != null;
    }

    public boolean isResourceBasedError() {
        return hasResource() && hasAction();
    }

    public String getActionDisplayText() {
        if (action == null) {
            return "perform action";
        }
        switch (action.toLowerCase()) {
            case "access": return "access";
            case "view": return "view";
            case "edit": return "edit";
            case "delete": return "delete";
            case "create": return "create";
            case "update": return "update";
            case "cancel": return "cancel";
            case "refund": return "process refund";
            default: return action;
        }
    }

    public String getRoleDisplayText(String role) {
        if (role == null) {
            return "Unknown";
        }
        switch (role.toUpperCase()) {
            case "ADMIN": return "Administrator";
            case "CUSTOMER": return "Customer";
            case "USER": return "User";
            case "MODERATOR": return "Moderator";
            case "MANAGER": return "Manager";
            default: return role;
        }
    }

    public String getUserFriendlyMessage() {
        if (isRoleBasedError()) {
            return String.format(
                    "You don't have permission to perform this action. Required role: %s",
                    getRoleDisplayText(requiredRole)
            );
        } else if (isResourceBasedError()) {
            return String.format(
                    "You don't have permission to %s %s",
                    getActionDisplayText(),
                    resource
            );
        } else {
            return "You don't have permission to perform this action";
        }
    }

    public String getDetailedMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Unauthorized access");

        if (hasUserId()) {
            message.append(" by user ").append(userId);
        }

        if (hasResource()) {
            message.append(" to resource: ").append(resource);
        }

        if (hasAction()) {
            message.append(" for action: ").append(action);
        }

        if (hasRoleInfo()) {
            if (requiredRole != null) {
                message.append(" (Required role: ").append(requiredRole);
            }
            if (currentRole != null) {
                message.append(", Current role: ").append(currentRole);
            }
            if (requiredRole != null) {
                message.append(")");
            }
        }

        return message.toString();
    }

    public String getSecurityLogMessage() {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("SECURITY: Unauthorized access attempt");

        if (hasUserId()) {
            logMessage.append(" - User: ").append(userId);
        }

        if (hasResource() && hasAction()) {
            logMessage.append(" - Attempted to ").append(action).append(" ").append(resource);
        }

        if (hasRoleInfo()) {
            logMessage.append(" - Required: ").append(requiredRole)
                    .append(", Current: ").append(currentRole);
        }

        return logMessage.toString();
    }

    // Helper methods for message formatting
    private static String formatMessage(String userId, String resource, String action) {
        if (userId != null && resource != null && action != null) {
            return String.format("User %s is not authorized to %s %s", userId, action, resource);
        } else if (resource != null && action != null) {
            return String.format("Not authorized to %s %s", action, resource);
        } else {
            return "Access denied";
        }
    }

    private static String formatMessageWithRoles(String userId, String resource, String action,
                                                 String requiredRole, String currentRole) {
        StringBuilder message = new StringBuilder();
        message.append("Access denied");

        if (userId != null) {
            message.append(" for user ").append(userId);
        }

        if (resource != null && action != null) {
            message.append(" to ").append(action).append(" ").append(resource);
        }

        if (requiredRole != null && currentRole != null) {
            message.append(". Required role: ").append(requiredRole)
                    .append(", Current role: ").append(currentRole);
        }

        return message.toString();
    }

    @Override
    public String toString() {
        return "UnauthorizedAccessException{" +
                "userId='" + userId + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", requiredRole='" + requiredRole + '\'' +
                ", currentRole='" + currentRole + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}