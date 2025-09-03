package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CustomerRegistrationDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email address too long")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number too long")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Optional marketing preferences
    private Boolean subscribeToNewsletter = false;
    private Boolean acceptMarketingEmails = false;
    private Boolean acceptSmsMarketing = false;

    // Terms and conditions
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTermsAndConditions;

    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacyPolicy;

    // Optional referral information
    @Size(max = 100, message = "Referral code too long")
    private String referralCode;

    @Size(max = 100, message = "How did you hear about us response too long")
    private String howDidYouHearAboutUs;

    // Default constructor
    public CustomerRegistrationDTO() {}

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getSubscribeToNewsletter() {
        return subscribeToNewsletter;
    }

    public void setSubscribeToNewsletter(Boolean subscribeToNewsletter) {
        this.subscribeToNewsletter = subscribeToNewsletter;
    }

    public Boolean getAcceptMarketingEmails() {
        return acceptMarketingEmails;
    }

    public void setAcceptMarketingEmails(Boolean acceptMarketingEmails) {
        this.acceptMarketingEmails = acceptMarketingEmails;
    }

    public Boolean getAcceptSmsMarketing() {
        return acceptSmsMarketing;
    }

    public void setAcceptSmsMarketing(Boolean acceptSmsMarketing) {
        this.acceptSmsMarketing = acceptSmsMarketing;
    }

    public Boolean getAcceptTermsAndConditions() {
        return acceptTermsAndConditions;
    }

    public void setAcceptTermsAndConditions(Boolean acceptTermsAndConditions) {
        this.acceptTermsAndConditions = acceptTermsAndConditions;
    }

    public Boolean getAcceptPrivacyPolicy() {
        return acceptPrivacyPolicy;
    }

    public void setAcceptPrivacyPolicy(Boolean acceptPrivacyPolicy) {
        this.acceptPrivacyPolicy = acceptPrivacyPolicy;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getHowDidYouHearAboutUs() {
        return howDidYouHearAboutUs;
    }

    public void setHowDidYouHearAboutUs(String howDidYouHearAboutUs) {
        this.howDidYouHearAboutUs = howDidYouHearAboutUs;
    }

    // Validation methods
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public boolean isMinor() {
        return getAge() < 18;
    }

    public boolean hasMarketingConsent() {
        return Boolean.TRUE.equals(acceptMarketingEmails) ||
                Boolean.TRUE.equals(acceptSmsMarketing) ||
                Boolean.TRUE.equals(subscribeToNewsletter);
    }

    // Security method to clear sensitive data
    public void clearSensitiveData() {
        this.password = null;
        this.confirmPassword = null;
    }

    @Override
    public String toString() {
        return "CustomerRegistrationDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", subscribeToNewsletter=" + subscribeToNewsletter +
                ", acceptTermsAndConditions=" + acceptTermsAndConditions +
                '}';
    }
}