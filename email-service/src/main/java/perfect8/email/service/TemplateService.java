package perfect8.email.service;

import perfect8.email.exception.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateEngine templateEngine;

    private static final Map<String, String> TEMPLATE_MAPPING = new HashMap<>();

    static {
        // Initialize template mappings
        TEMPLATE_MAPPING.put("order-confirmation", "order-confirmation");
        TEMPLATE_MAPPING.put("shipping-notification", "shipping-notification");
        TEMPLATE_MAPPING.put("password-reset", "password-reset");
        TEMPLATE_MAPPING.put("welcome", "welcome");
        TEMPLATE_MAPPING.put("newsletter", "newsletter");
        TEMPLATE_MAPPING.put("promotion", "promotion");
        TEMPLATE_MAPPING.put("abandoned-cart", "abandoned-cart");
        TEMPLATE_MAPPING.put("review-request", "review-request");
        TEMPLATE_MAPPING.put("payment-received", "payment-received");
        TEMPLATE_MAPPING.put("refund-processed", "refund-processed");
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            if (!TEMPLATE_MAPPING.containsKey(templateName)) {
                throw new EmailException(EmailException.ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found: " + templateName);
            }

            Context context = new Context();

            // Add default variables
            context.setVariable("companyName", "Perfect8");
            context.setVariable("companyLogo", "https://perfect8.com/logo.png");
            context.setVariable("currentYear", Calendar.getInstance().get(Calendar.YEAR));
            context.setVariable("supportEmail", "support@perfect8.com");
            context.setVariable("websiteUrl", "https://perfect8.com");

            // Add custom variables
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String actualTemplateName = TEMPLATE_MAPPING.get(templateName);
            log.debug("Processing template: {} with variables: {}", actualTemplateName, variables);

            return templateEngine.process("emails/" + actualTemplateName, context);
        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            throw new EmailException(EmailException.ErrorCode.TEMPLATE_PROCESSING_ERROR,
                    "Failed to process template: " + templateName, e);
        }
    }

    public List<String> getAvailableTemplates() {
        return new ArrayList<>(TEMPLATE_MAPPING.keySet());
    }

    public String processOrderConfirmationTemplate(Map<String, Object> orderData) {
        Map<String, Object> variables = new HashMap<>(orderData);

        // Ensure required fields
        validateRequiredFields(variables, Arrays.asList("orderId", "customerName", "orderTotal", "items"));

        // Add additional formatting
        if (variables.get("orderTotal") instanceof Number) {
            Number total = (Number) variables.get("orderTotal");
            variables.put("formattedOrderTotal", String.format("$%.2f", total.doubleValue()));
        }

        return processTemplate("order-confirmation", variables);
    }

    public String processShippingNotificationTemplate(Map<String, Object> shippingData) {
        Map<String, Object> variables = new HashMap<>(shippingData);

        // Ensure required fields
        validateRequiredFields(variables, Arrays.asList("orderId", "customerName", "trackingNumber", "carrier"));

        // Add tracking URL based on carrier
        String carrier = (String) variables.get("carrier");
        String trackingNumber = (String) variables.get("trackingNumber");
        variables.put("trackingUrl", generateTrackingUrl(carrier, trackingNumber));

        return processTemplate("shipping-notification", variables);
    }

    public String processPasswordResetTemplate(String userName, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("resetLink", "https://perfect8.com/reset-password?token=" + resetToken);
        variables.put("expirationHours", 24);

        return processTemplate("password-reset", variables);
    }

    private void validateRequiredFields(Map<String, Object> variables, List<String> requiredFields) {
        for (String field : requiredFields) {
            if (!variables.containsKey(field) || variables.get(field) == null) {
                throw new EmailException(EmailException.ErrorCode.TEMPLATE_PROCESSING_ERROR,
                        "Required field missing: " + field);
            }
        }
    }

    private String generateTrackingUrl(String carrier, String trackingNumber) {
        return switch (carrier.toUpperCase()) {
            case "USPS" -> "https://tools.usps.com/go/TrackConfirmAction?tLabels=" + trackingNumber;
            case "UPS" -> "https://www.ups.com/track?tracknum=" + trackingNumber;
            case "FEDEX" -> "https://www.fedex.com/fedextrack/?tracknumbers=" + trackingNumber;
            case "DHL" -> "https://www.dhl.com/en/express/tracking.html?AWB=" + trackingNumber;
            default -> "https://perfect8.com/track-order?tracking=" + trackingNumber;
        };
    }
}