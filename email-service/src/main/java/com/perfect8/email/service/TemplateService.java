package com.perfect8.email.service;

import com.perfect8.email.config.EmailConfig;
import com.perfect8.email.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Template processing service for email templates without Thymeleaf
 * Uses simple string replacement for template variables
 * Version 1.0 - Core template functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final EmailConfig.EmailTemplateConfig templateConfig;

    private static final Map<String, String> TEMPLATE_MAPPING = new HashMap<>();

    static {
        // Initialize template mappings - Version 1.0 templates only
        TEMPLATE_MAPPING.put("order-confirmation", "order-confirmation");
        TEMPLATE_MAPPING.put("order-shipped", "order-shipped");
        TEMPLATE_MAPPING.put("order-cancelled", "order-cancelled");
        TEMPLATE_MAPPING.put("password-reset", "password-reset");
        TEMPLATE_MAPPING.put("welcome", "welcome");
        TEMPLATE_MAPPING.put("newsletter", "newsletter");
        TEMPLATE_MAPPING.put("new-post-notification", "new-post-notification");
    }

    /**
     * Process a template with the given variables
     */
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            if (!TEMPLATE_MAPPING.containsKey(templateName)) {
                throw new EmailException(EmailException.ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found: " + templateName);
            }

            // Get the base template
            String template = getBaseTemplate(templateName);

            // Add default variables
            Map<String, Object> allVariables = new HashMap<>();
            allVariables.put("companyName", templateConfig.getCompanyName());
            allVariables.put("companyLogo", templateConfig.getLogoUrl());
            allVariables.put("currentYear", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            allVariables.put("supportEmail", templateConfig.getSupportEmail());
            allVariables.put("websiteUrl", templateConfig.getFrontendUrl());
            allVariables.put("unsubscribeUrl", templateConfig.getFrontendUrl() + "/unsubscribe");
            allVariables.put("primaryColor", templateConfig.getPrimaryColor());
            allVariables.put("footerText", templateConfig.getFooterText());

            // Add custom variables
            if (variables != null) {
                allVariables.putAll(variables);
            }

            // Replace variables in template
            String processedTemplate = replaceVariables(template, allVariables);

            log.debug("Processed template: {} with {} variables", templateName, allVariables.size());

            return processedTemplate;

        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            throw new EmailException(EmailException.ErrorCode.TEMPLATE_PROCESSING_ERROR,
                    "Failed to process template: " + templateName, e);
        }
    }

    /**
     * Get available template names
     */
    public List<String> getAvailableTemplates() {
        return new ArrayList<>(TEMPLATE_MAPPING.keySet());
    }

    /**
     * Process order confirmation template
     */
    public String processOrderConfirmationTemplate(Map<String, Object> orderData) {
        Map<String, Object> variables = new HashMap<>(orderData);

        // Ensure required fields
        validateRequiredFields(variables, Arrays.asList("orderId", "customerName", "orderTotal", "items"));

        // Format order total
        if (variables.get("orderTotal") instanceof Number) {
            Number total = (Number) variables.get("orderTotal");
            variables.put("formattedOrderTotal", String.format("$%.2f", total.doubleValue()));
        }

        // Format order date
        variables.put("orderDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        return processTemplate("order-confirmation", variables);
    }

    /**
     * Process shipping notification template
     */
    public String processShippingNotificationTemplate(Map<String, Object> shippingData) {
        Map<String, Object> variables = new HashMap<>(shippingData);

        validateRequiredFields(variables, Arrays.asList("orderId", "customerName", "trackingNumber", "carrier"));

        String carrier = (String) variables.get("carrier");
        String trackingNumber = (String) variables.get("trackingNumber");
        variables.put("trackingUrl", generateTrackingUrl(carrier, trackingNumber));

        return processTemplate("order-shipped", variables);
    }

    /**
     * Process password reset template
     */
    public String processPasswordResetTemplate(String userName, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("resetLink", templateConfig.getFrontendUrl() + "/reset-password?token=" + resetToken);
        variables.put("expirationHours", "24");

        return processTemplate("password-reset", variables);
    }

    /**
     * Get base template HTML
     */
    private String getBaseTemplate(String templateName) {
        // Since we can't use Thymeleaf or read from files, we'll use hardcoded templates
        // In production, these could be loaded from database or configuration

        return switch (templateName) {
            case "order-confirmation" -> getOrderConfirmationTemplate();
            case "order-shipped" -> getOrderShippedTemplate();
            case "order-cancelled" -> getOrderCancelledTemplate();
            case "password-reset" -> getPasswordResetTemplate();
            case "welcome" -> getWelcomeTemplate();
            case "newsletter" -> getNewsletterTemplate();
            case "new-post-notification" -> getNewPostNotificationTemplate();
            default -> getDefaultTemplate();
        };
    }

    /**
     * Replace variables in template using ${variableName} syntax
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        // Remove any remaining placeholders
        result = result.replaceAll("\\$\\{[^}]+\\}", "");

        return result;
    }

    /**
     * Validate required fields
     */
    private void validateRequiredFields(Map<String, Object> variables, List<String> requiredFields) {
        for (String field : requiredFields) {
            if (!variables.containsKey(field) || variables.get(field) == null) {
                throw new EmailException(EmailException.ErrorCode.TEMPLATE_PROCESSING_ERROR,
                        "Required field missing: " + field);
            }
        }
    }

    /**
     * Generate tracking URL based on carrier
     */
    private String generateTrackingUrl(String carrier, String trackingNumber) {
        if (carrier == null || trackingNumber == null) {
            return templateConfig.getFrontendUrl() + "/track-order";
        }

        return switch (carrier.toUpperCase()) {
            case "USPS" -> "https://tools.usps.com/go/TrackConfirmAction?tLabels=" + trackingNumber;
            case "UPS" -> "https://www.ups.com/track?tracknum=" + trackingNumber;
            case "FEDEX" -> "https://www.fedex.com/fedextrack/?tracknumbers=" + trackingNumber;
            case "DHL" -> "https://www.dhl.com/en/express/tracking.html?AWB=" + trackingNumber;
            case "POSTNORD" -> "https://www.postnord.se/vara-verktyg/spara-brev-paket-och-pall?trackingnumber=" + trackingNumber;
            default -> templateConfig.getFrontendUrl() + "/track-order?tracking=" + trackingNumber;
        };
    }

    // Template HTML strings (simplified for v1.0)

    private String getOrderConfirmationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: ${primaryColor}; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .order-box { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${companyName}</h1>
                </div>
                <div class="content">
                    <h2>Order Confirmation</h2>
                    <p>Dear ${customerName},</p>
                    <p>Thank you for your order! Your order #${orderId} has been confirmed.</p>
                    <div class="order-box">
                        <h3>Order Details:</h3>
                        <p><strong>Order ID:</strong> ${orderId}</p>
                        <p><strong>Order Date:</strong> ${orderDate}</p>
                        <p><strong>Total Amount:</strong> ${formattedOrderTotal}</p>
                    </div>
                    <p>We'll send you another email when your order ships.</p>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                    <p><a href="${unsubscribeUrl}">Unsubscribe</a> | <a href="${websiteUrl}">Visit our website</a></p>
                </div>
            </body>
            </html>
            """;
    }

    private String getOrderShippedTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: ${primaryColor}; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .tracking-box { background-color: #e7f3ff; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .button { background-color: ${primaryColor}; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${companyName}</h1>
                </div>
                <div class="content">
                    <h2>Your Order Has Shipped!</h2>
                    <p>Dear ${customerName},</p>
                    <p>Great news! Your order #${orderId} has been shipped.</p>
                    <div class="tracking-box">
                        <h3>Tracking Information:</h3>
                        <p><strong>Tracking Number:</strong> ${trackingNumber}</p>
                        <p><strong>Carrier:</strong> ${carrier}</p>
                        <p><a href="${trackingUrl}" class="button">Track Your Package</a></p>
                    </div>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }

    private String getOrderCancelledTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .cancel-box { background-color: #f8d7da; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${companyName}</h1>
                </div>
                <div class="content">
                    <h2>Order Cancelled</h2>
                    <p>Dear ${customerName},</p>
                    <p>Your order #${orderId} has been cancelled.</p>
                    <div class="cancel-box">
                        <p><strong>Refund Amount:</strong> ${formattedRefundAmount}</p>
                        <p>Your refund will be processed within 3-5 business days.</p>
                    </div>
                    <p>If you have any questions, please contact our support team.</p>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }

    private String getPasswordResetTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #ffc107; color: #212529; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .reset-box { background-color: #fff3cd; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .button { background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Password Reset Request</h1>
                </div>
                <div class="content">
                    <p>Hello ${userName},</p>
                    <p>We received a request to reset your password.</p>
                    <div class="reset-box">
                        <p>Click the button below to reset your password:</p>
                        <p><a href="${resetLink}" class="button">Reset Password</a></p>
                        <p><small>This link expires in ${expirationHours} hours.</small></p>
                    </div>
                    <p>If you didn't request this, please ignore this email.</p>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }

    private String getWelcomeTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .welcome-box { background-color: #d4edda; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Welcome to ${companyName}!</h1>
                </div>
                <div class="content">
                    <p>Dear ${customerName},</p>
                    <p>Thank you for joining ${companyName}! We're excited to have you.</p>
                    <div class="welcome-box">
                        <h3>What's Next?</h3>
                        <ul>
                            <li>Browse our latest products</li>
                            <li>Enjoy member-exclusive discounts</li>
                            <li>Get personalized recommendations</li>
                        </ul>
                    </div>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }

    private String getNewsletterTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: ${primaryColor}; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${companyName} Newsletter</h1>
                </div>
                <div class="content">
                    <h2>${title}</h2>
                    ${content}
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                    <p><a href="${unsubscribeUrl}">Unsubscribe</a></p>
                </div>
            </body>
            </html>
            """;
    }

    private String getNewPostNotificationTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: ${primaryColor}; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .post-box { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .button { background-color: ${primaryColor}; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>New Blog Post</h1>
                </div>
                <div class="content">
                    <div class="post-box">
                        <h2>${postTitle}</h2>
                        <p>${postExcerpt}</p>
                        <p><a href="${postUrl}" class="button">Read More</a></p>
                    </div>
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }

    private String getDefaultTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                    .header { background-color: ${primaryColor}; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${companyName}</h1>
                </div>
                <div class="content">
                    ${content}
                </div>
                <div class="footer">
                    <p>${footerText}</p>
                </div>
            </body>
            </html>
            """;
    }
}