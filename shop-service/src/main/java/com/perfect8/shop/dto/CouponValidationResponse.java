package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {

    private Boolean isValid;
    private String couponCode;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private String discountType;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isExpired;
    private Boolean isNotYetActive;

    private Integer usageLimit;
    private Integer currentUsageCount;
    private Boolean isUsageLimitReached;

    private Integer perCustomerLimit;
    private Integer customerUsageCount;
    private Boolean isCustomerLimitReached;

    private List<String> applicableProductIds;
    private List<String> applicableCategoryIds;
    private List<String> excludedProductIds;
    private List<String> excludedCategoryIds;

    private Boolean isFirstTimeCustomerOnly;
    private Boolean customerEligible;

    private String errorCode;
    private String errorMessage;
    private List<String> validationErrors;

    // Constructor with boolean and message for backward compatibility
    public CouponValidationResponse(Boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    // Static factory methods for common scenarios
    public static CouponValidationResponse valid(String couponCode, BigDecimal discountAmount, String message) {
        return CouponValidationResponse.builder()
                .isValid(true)
                .couponCode(couponCode)
                .discountAmount(discountAmount)
                .message(message)
                .build();
    }

    public static CouponValidationResponse invalid(String couponCode, String errorMessage) {
        return CouponValidationResponse.builder()
                .isValid(false)
                .couponCode(couponCode)
                .errorMessage(errorMessage)
                .message(errorMessage)
                .build();
    }

    public static CouponValidationResponse expired(String couponCode) {
        return CouponValidationResponse.builder()
                .isValid(false)
                .couponCode(couponCode)
                .isExpired(true)
                .errorMessage("Coupon has expired")
                .message("Coupon has expired")
                .build();
    }

    public static CouponValidationResponse usageLimitReached(String couponCode) {
        return CouponValidationResponse.builder()
                .isValid(false)
                .couponCode(couponCode)
                .isUsageLimitReached(true)
                .errorMessage("Coupon usage limit has been reached")
                .message("Coupon usage limit has been reached")
                .build();
    }
}