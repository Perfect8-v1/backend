package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class RefundRequestDTO {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Refund amount must be a valid monetary value")
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    @Size(min = 10, max = 500, message = "Refund reason must be between 10 and 500 characters")
    private String reason;

    @Pattern(regexp = "FULL|PARTIAL", message = "Refund type must be FULL or PARTIAL")
    private String refundType = "FULL";

    @Pattern(regexp = "DEFECTIVE|DAMAGED|WRONG_ITEM|NOT_AS_DESCRIBED|CUSTOMER_CHANGED_MIND|DUPLICATE_ORDER|CANCELED_ORDER|OTHER",
            message = "Invalid refund category")
    private String refundCategory;

    @Size(max = 1000, message = "Additional notes too long")
    private String additionalNotes;

    // Customer contact information for refund processing
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email too long")
    private String customerEmail;

    @Size(max = 20, message = "Phone number too long")
    private String customerPhone;

    // Refund method preference
    @Pattern(regexp = "ORIGINAL_METHOD|BANK_TRANSFER|STORE_CREDIT|CHECK",
            message = "Invalid refund method")
    private String preferredRefundMethod = "ORIGINAL_METHOD";

    // Bank details for bank transfer refunds
    @Size(max = 100, message = "Bank name too long")
    private String bankName;

    @Size(max = 50, message = "Account number too long")
    private String accountNumber;

    @Size(max = 20, message = "Routing number too long")
    private String routingNumber;

    @Size(max = 100, message = "Account holder name too long")
    private String accountHolderName;

    // For partial refunds - specify which items
    private List<RefundItemDTO> refundItems;

    // Processing options
    private Boolean expediteProcessing = false;
    private Boolean sendEmailConfirmation = true;
    private Boolean createStoreCredit = false;

    // Return merchandise authorization
    @Size(max = 50, message = "RMA number too long")
    private String rmaNumber;

    private Boolean returnRequested = false;

    @Size(max = 500, message = "Return shipping address too long")
    private String returnShippingAddress;

    // Supporting evidence
    private List<String> supportingDocuments; // URLs to uploaded images/documents

    @Size(max = 200, message = "Evidence description too long")
    private String evidenceDescription;

    // Default constructor
    public RefundRequestDTO() {}

    // Constructor with required fields
    public RefundRequestDTO(BigDecimal amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public String getRefundCategory() {
        return refundCategory;
    }

    public void setRefundCategory(String refundCategory) {
        this.refundCategory = refundCategory;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPreferredRefundMethod() {
        return preferredRefundMethod;
    }

    public void setPreferredRefundMethod(String preferredRefundMethod) {
        this.preferredRefundMethod = preferredRefundMethod;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public List<RefundItemDTO> getRefundItems() {
        return refundItems;
    }

    public void setRefundItems(List<RefundItemDTO> refundItems) {
        this.refundItems = refundItems;
    }

    public Boolean getExpediteProcessing() {
        return expediteProcessing;
    }

    public void setExpediteProcessing(Boolean expediteProcessing) {
        this.expediteProcessing = expediteProcessing;
    }

    public Boolean getSendEmailConfirmation() {
        return sendEmailConfirmation;
    }

    public void setSendEmailConfirmation(Boolean sendEmailConfirmation) {
        this.sendEmailConfirmation = sendEmailConfirmation;
    }

    public Boolean getCreateStoreCredit() {
        return createStoreCredit;
    }

    public void setCreateStoreCredit(Boolean createStoreCredit) {
        this.createStoreCredit = createStoreCredit;
    }

    public String getRmaNumber() {
        return rmaNumber;
    }

    public void setRmaNumber(String rmaNumber) {
        this.rmaNumber = rmaNumber;
    }

    public Boolean getReturnRequested() {
        return returnRequested;
    }

    public void setReturnRequested(Boolean returnRequested) {
        this.returnRequested = returnRequested;
    }

    public String getReturnShippingAddress() {
        return returnShippingAddress;
    }

    public void setReturnShippingAddress(String returnShippingAddress) {
        this.returnShippingAddress = returnShippingAddress;
    }

    public List<String> getSupportingDocuments() {
        return supportingDocuments;
    }

    public void setSupportingDocuments(List<String> supportingDocuments) {
        this.supportingDocuments = supportingDocuments;
    }

    public String getEvidenceDescription() {
        return evidenceDescription;
    }

    public void setEvidenceDescription(String evidenceDescription) {
        this.evidenceDescription = evidenceDescription;
    }

    // Utility methods
    public boolean isFullRefund() {
        return "FULL".equals(refundType);
    }

    public boolean isPartialRefund() {
        return "PARTIAL".equals(refundType);
    }

    public boolean requiresBankDetails() {
        return "BANK_TRANSFER".equals(preferredRefundMethod);
    }

    public boolean hasValidBankDetails() {
        return bankName != null && !bankName.trim().isEmpty() &&
                accountNumber != null && !accountNumber.trim().isEmpty() &&
                accountHolderName != null && !accountHolderName.trim().isEmpty();
    }

    public boolean hasSupportingEvidence() {
        return (supportingDocuments != null && !supportingDocuments.isEmpty()) ||
                (evidenceDescription != null && !evidenceDescription.trim().isEmpty());
    }

    public boolean isDefectiveItemRefund() {
        return "DEFECTIVE".equals(refundCategory) || "DAMAGED".equals(refundCategory);
    }

    public boolean isCustomerFaultRefund() {
        return "CUSTOMER_CHANGED_MIND".equals(refundCategory);
    }

    public int getTotalRefundItems() {
        return refundItems != null ? refundItems.size() : 0;
    }

    public BigDecimal getTotalRefundItemsAmount() {
        if (refundItems == null || refundItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return refundItems.stream()
                .map(RefundItemDTO::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Inner class for refund items
    public static class RefundItemDTO {
        @NotNull(message = "Order item ID is required")
        private Long orderItemId;

        @NotNull(message = "Refund quantity is required")
        @Min(value = 1, message = "Refund quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Refund amount is required")
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        private BigDecimal refundAmount;

        @Size(max = 200, message = "Item refund reason too long")
        private String itemRefundReason;

        // Default constructor
        public RefundItemDTO() {}

        // Constructor
        public RefundItemDTO(Long orderItemId, Integer quantity, BigDecimal refundAmount) {
            this.orderItemId = orderItemId;
            this.quantity = quantity;
            this.refundAmount = refundAmount;
        }

        // Getters and Setters
        public Long getOrderItemId() {
            return orderItemId;
        }

        public void setOrderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getItemRefundReason() {
            return itemRefundReason;
        }

        public void setItemRefundReason(String itemRefundReason) {
            this.itemRefundReason = itemRefundReason;
        }
    }

    @Override
    public String toString() {
        return "RefundRequestDTO{" +
                "amount=" + amount +
                ", reason='" + reason + '\'' +
                ", refundType='" + refundType + '\'' +
                ", refundCategory='" + refundCategory + '\'' +
                ", preferredRefundMethod='" + preferredRefundMethod + '\'' +
                ", expediteProcessing=" + expediteProcessing +
                '}';
    }
}