package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CustomerUpdateDTO {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number too long")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Marketing preferences
    private Boolean subscribeToNewsletter;
    private Boolean acceptMarketingEmails;
    private Boolean acceptSmsMarketing;

    // Profile preferences
    @Size(max = 10, message = "Language code too long")
    private String preferredLanguage;

    @Size(max = 5, message = "Currency code too long")
    private String preferredCurrency;

    @Size(max = 50, message = "Timezone too long")
    private String timezone;

    // Communication preferences
    private Boolean emailOrderConfirmations;
    private Boolean smsOrderUpdates;
    private Boolean emailPromotions;

    // Security settings
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String newPassword;

    private String confirmNewPassword;

    @Size(max = 100, message = "Current password too long")
    private String currentPassword;

    // Profile information
    @Size(max = 10, message = "Gender specification too long")
    private String gender;

    @Size(max = 100, message = "Occupation too long")
    private String occupation;

    @Size(max = 500, message = "Bio too long")
    private String bio;

    // Default constructor
    public CustomerUpdateDTO() {}

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

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getEmailOrderConfirmations() {
        return emailOrderConfirmations;
    }

    public void setEmailOrderConfirmations(Boolean emailOrderConfirmations) {
        this.emailOrderConfirmations = emailOrderConfirmations;
    }

    public Boolean getSmsOrderUpdates() {
        return smsOrderUpdates;
    }

    public void setSmsOrderUpdates(Boolean smsOrderUpdates) {
        this.smsOrderUpdates = smsOrderUpdates;
    }

    public Boolean getEmailPromotions() {
        return emailPromotions;
    }

    public void setEmailPromotions(Boolean emailPromotions) {
        this.emailPromotions = emailPromotions;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // Validation methods
    @AssertTrue(message = "New passwords do not match")
    public boolean isNewPasswordMatching() {
        if (newPassword == null && confirmNewPassword == null) {
            return true; // No password change
        }
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }

    // Utility methods
    public boolean isPasswordChangeRequested() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }

    public boolean hasMarketingPreferenceChanges() {
        return subscribeToNewsletter != null ||
                acceptMarketingEmails != null ||
                acceptSmsMarketing != null ||
                emailPromotions != null;
    }

    public boolean hasCommunicationPreferenceChanges() {
        return emailOrderConfirmations != null ||
                smsOrderUpdates != null ||
                emailPromotions != null;
    }

    public boolean hasProfileChanges() {
        return firstName != null ||
                lastName != null ||
                phoneNumber != null ||
                dateOfBirth != null ||
                gender != null ||
                occupation != null ||
                bio != null;
    }

    public boolean hasPreferenceChanges() {
        return preferredLanguage != null ||
                preferredCurrency != null ||
                timezone != null;
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return null;
    }

    // Security method to clear sensitive data
    public void clearSensitiveData() {
        this.newPassword = null;
        this.confirmNewPassword = null;
        this.currentPassword = null;
    }

    @Override
    public String toString() {
        return "CustomerUpdateDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", subscribeToNewsletter=" + subscribeToNewsletter +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", hasPasswordChange=" + isPasswordChangeRequested() +
                '}';
    }
}