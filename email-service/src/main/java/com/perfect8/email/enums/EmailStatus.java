package com.perfect8.email.enums;

import lombok.Getter;

/**
 * Email status enumeration
 * Version 1.0 - Core email statuses
 */
@Getter
public enum EmailStatus {

    // Initial states
    DRAFT("Draft", "Email is being composed", false, false),
    QUEUED("Queued", "Email is queued for sending", false, false),
    SCHEDULED("Scheduled", "Email is scheduled for future delivery", false, false),

    // Processing states
    SENDING("Sending", "Email is being sent", false, false),
    RETRYING("Retrying", "Email send is being retried", false, true),

    // Success states
    SENT("Sent", "Email has been sent successfully", true, false),
    DELIVERED("Delivered", "Email has been delivered to recipient", true, false),
    ACCEPTED("Accepted", "Email accepted by recipient server", true, false),

    // Failure states
    FAILED("Failed", "Email send failed", false, true),
    BOUNCED("Bounced", "Email bounced back", false, true),
    REJECTED("Rejected", "Email rejected by recipient server", false, true),
    BLOCKED("Blocked", "Email blocked by spam filter", false, true),

    // User action states
    COMPLAINED("Complained", "Recipient marked as spam", false, true),
    UNSUBSCRIBED("Unsubscribed", "Recipient unsubscribed", false, true),

    // Cancellation
    CANCELLED("Cancelled", "Email was cancelled", false, false),

    // Version 2.0 states - commented out
    // OPENED("Opened", "Email has been opened", true, false),
    // CLICKED("Clicked", "Link in email was clicked", true, false),
    // CONVERTED("Converted", "Email led to conversion", true, false),
    ;

    private final String displayName;
    private final String description;
    private final boolean successState;
    private final boolean errorState;

    EmailStatus(String displayName, String description, boolean successState, boolean errorState) {
        this.displayName = displayName;
        this.description = description;
        this.successState = successState;
        this.errorState = errorState;
    }

    public boolean isFinalState() {
        return this == DELIVERED || this == FAILED || this == BOUNCED ||
                this == REJECTED || this == BLOCKED || this == COMPLAINED ||
                this == UNSUBSCRIBED || this == CANCELLED;
    }

    public boolean isRetryableState() {
        return this == FAILED || this == RETRYING;
    }

    public boolean isPendingState() {
        return this == QUEUED || this == SCHEDULED || this == SENDING || this == RETRYING;
    }

    public boolean isSuccessState() {
        return successState;
    }

    public boolean isErrorState() {
        return errorState;
    }

    public boolean canTransitionTo(EmailStatus newStatus) {
        // Define valid status transitions
        switch (this) {
            case DRAFT:
                return newStatus == QUEUED || newStatus == SCHEDULED || newStatus == CANCELLED;
            case QUEUED:
                return newStatus == SENDING || newStatus == CANCELLED || newStatus == FAILED;
            case SCHEDULED:
                return newStatus == QUEUED || newStatus == CANCELLED;
            case SENDING:
                return newStatus == SENT || newStatus == FAILED || newStatus == RETRYING;
            case RETRYING:
                return newStatus == SENT || newStatus == FAILED;
            case SENT:
                return newStatus == DELIVERED || newStatus == BOUNCED || newStatus == REJECTED;
            case DELIVERED:
                return newStatus == COMPLAINED || newStatus == UNSUBSCRIBED;
            case FAILED:
            case BOUNCED:
            case REJECTED:
            case BLOCKED:
            case COMPLAINED:
            case UNSUBSCRIBED:
            case CANCELLED:
                return false; // These are final states
            default:
                return false;
        }
    }

    public static EmailStatus fromString(String status) {
        if (status == null) {
            return null;
        }

        for (EmailStatus es : EmailStatus.values()) {
            if (es.name().equalsIgnoreCase(status) || es.displayName.equalsIgnoreCase(status)) {
                return es;
            }
        }

        throw new IllegalArgumentException("Unknown email status: " + status);
    }
}