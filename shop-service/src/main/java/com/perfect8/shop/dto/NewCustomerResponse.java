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
public class NewCustomerResponse {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime registrationDate;
    private String registrationSource;
    private String referralCode;
    private String referredBy;
    private String customerType;
    private String accountStatus;
    private Boolean isActive;
    private Boolean isVerified;
    private String verificationStatus;
    private String country;
    private String state;
    private String city;
    private String postalCode;
    private String timezone;
    private String preferredLanguage;
    private String preferredCurrency;
    private String marketingOptIn;
    private Boolean hasCompletedProfile;
    private Boolean hasPlacedOrder;
    private Integer daysSinceRegistration;
    private BigDecimal firstOrderValue;
    private LocalDateTime firstOrderDate;
    private String acquisitionChannel;
    private String campaignSource;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private List<String> interests;
    private List<String> preferences;
    private String deviceType;
    private String browserInfo;
    private String ipAddress;
    private BigDecimal lifetimeValue;
    private Integer totalOrders;
    private String riskLevel;
    private Boolean requiresWelcomeEmail;
    private Boolean requiresOnboarding;
    private String assignedSalesRep;
    private String customerSegment;
    private LocalDateTime lastActivityDate;
    private String notes;
}
