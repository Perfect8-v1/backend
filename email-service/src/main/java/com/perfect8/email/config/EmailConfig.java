package com.perfect8.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Email configuration for the email service
 * Version 1.0 - Core email functionality without Thymeleaf
 */
@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private boolean starttlsRequired;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean mailDebug;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}")
    private int connectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:5000}")
    private int timeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:5000}")
    private int writeTimeout;

    @Value("${app.company.name:Perfect8}")
    private String companyName;

    @Value("${app.company.support-email:support@perfect8.com}")
    private String supportEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Configure JavaMailSender with SMTP settings
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Basic mail server settings
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        // Additional properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", starttlsRequired);
        props.put("mail.debug", mailDebug);

        // Timeout settings
        props.put("mail.smtp.connectiontimeout", connectionTimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writeTimeout);

        // Additional security and encoding settings
        props.put("mail.smtp.ssl.trust", mailHost);
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.smtp.allow8bitmime", "true");
        props.put("mail.smtp.socketFactory.fallback", "false");

        // Set default encoding
        mailSender.setDefaultEncoding("UTF-8");

        return mailSender;
    }

    /**
     * Email service configuration bean
     * Can be used to inject email-specific settings
     */
    @Bean
    public EmailServiceConfig emailServiceConfig() {
        return EmailServiceConfig.builder()
                .maxRetries(3)
                .retryDelaySeconds(60)
                .maxAttachmentSizeMB(10)
                .maxRecipientsPerEmail(50)
                .maxBulkEmailBatchSize(100)
                .bulkEmailDelaySeconds(1)
                .enableEmailLogging(true)
                .enableAsyncSending(true)
                .defaultFromAddress(mailUsername)
                .defaultFromName(companyName)
                .companyName(companyName)
                .supportEmail(supportEmail)
                .frontendUrl(frontendUrl)
                .build();
    }

    /**
     * Email template configuration for HTML template generation
     * Since we're not using Thymeleaf, templates will be handled with simple string replacement
     */
    @Bean
    public EmailTemplateConfig emailTemplateConfig() {
        return EmailTemplateConfig.builder()
                .companyName(companyName)
                .supportEmail(supportEmail)
                .frontendUrl(frontendUrl)
                .logoUrl(frontendUrl + "/logo.png")
                .primaryColor("#007bff")
                .secondaryColor("#6c757d")
                .successColor("#28a745")
                .dangerColor("#dc3545")
                .warningColor("#ffc107")
                .infoColor("#17a2b8")
                .build();
    }

    /**
     * Inner configuration class for email service settings
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmailServiceConfig {
        private int maxRetries;
        private int retryDelaySeconds;
        private int maxAttachmentSizeMB;
        private int maxRecipientsPerEmail;
        private int maxBulkEmailBatchSize;
        private int bulkEmailDelaySeconds;
        private boolean enableEmailLogging;
        private boolean enableAsyncSending;
        private String defaultFromAddress;
        private String defaultFromName;
        private String companyName;
        private String supportEmail;
        private String frontendUrl;

        // Version 2.0 settings - commented out
        // private boolean enableOpenTracking;
        // private boolean enableClickTracking;
        // private String trackingDomain;
        // private boolean enableBounceHandling;
        // private String bounceEmailAddress;
    }

    /**
     * Inner configuration class for email template settings
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmailTemplateConfig {
        private String companyName;
        private String supportEmail;
        private String frontendUrl;
        private String logoUrl;
        private String primaryColor;
        private String secondaryColor;
        private String successColor;
        private String dangerColor;
        private String warningColor;
        private String infoColor;

        // Footer settings
        @lombok.Builder.Default
        private String footerText = "© 2024 Perfect8. All rights reserved.";

        @lombok.Builder.Default
        private String unsubscribeText = "You received this email because you're subscribed to emails from Perfect8.";

        // Social media links (for v2.0)
        // private String facebookUrl;
        // private String twitterUrl;
        // private String instagramUrl;
        // private String linkedinUrl;
    }
}