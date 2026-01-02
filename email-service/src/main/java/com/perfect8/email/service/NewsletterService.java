package com.perfect8.email.service;

import com.perfect8.email.client.ShopServiceClient;
import com.perfect8.email.dto.CustomerEmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final EmailService emailService;
    private final ShopServiceClient shopServiceClient;

    // V1.0 - Segment ignoreras, skickar alltid till ALLA prenumeranter
    public void sendNewsletter(String subject, String content, String segment) {
        try {
            log.info("Preparing newsletter with subject: '{}', segment: '{}' (v1.0: ignored)",
                    subject, segment);

            // V1.0 - Hårdkodat: Hämta ALLA som prenumererar, ignorera segment
            // ShopServiceClient vill ha segment som parameter, vi skickar alltid "ALL"
            List<CustomerEmailDTO> subscribers = shopServiceClient.getNewsletterSubscribers("ALL");

            if (subscribers == null || subscribers.isEmpty()) {
                log.warn("No newsletter subscribers found");
                return;
            }

            log.info("Sending newsletter to {} subscribers (v1.0: ALL subscribers)",
                    subscribers.size());

            // V1.0 - Enkel bulk-sändning utan fancy tracking
            sendBulkNewsletterEmails(subscribers, subject, content);

            log.info("Newsletter dispatch completed for {} recipients", subscribers.size());

        } catch (Exception e) {
            log.error("Failed to send newsletter: {}", e.getMessage(), e);
            // V1.0 - Gmail hanterar retry, vi bara loggar
        }
    }

    // V1.0 - Bulk-sändning för kampanjer
    public void sendPromotionalEmail(String subject, String content, String promoCode) {
        try {
            log.info("Sending promotional email with code: {}", promoCode);

            // Hämta alla aktiva kunder
            // ShopServiceClient vill ha segment som parameter
            List<CustomerEmailDTO> customers = shopServiceClient.getActiveCustomers("ALL");

            if (customers == null || customers.isEmpty()) {
                log.warn("No active customers found for promotion");
                return;
            }

            // Lägg till promo-kod i innehållet
            String enhancedContent = content +
                    "<br><br><p><strong>Use promo code: " + promoCode + "</strong></p>";

            log.info("Sending promotion to {} customers", customers.size());
            sendBulkNewsletterEmails(customers, subject, enhancedContent);

        } catch (Exception e) {
            log.error("Failed to send promotional email: {}", e.getMessage());
        }
    }

    // V1.0 - Ny produkt-notifikation
    public void sendNewProductNotification(Long productId, String productName, String productDescription) {
        try {
            log.info("Sending new product notification for: {}", productName);

            // Hämta alla prenumeranter
            // ShopServiceClient vill ha segment som parameter
            List<CustomerEmailDTO> subscribers = shopServiceClient.getNewsletterSubscribers("ALL");

            if (subscribers == null || subscribers.isEmpty()) {
                log.warn("No subscribers for new product notification");
                return;
            }

            String subject = "New Product Alert: " + productName;
            String content = buildNewProductEmailContent(productName, productDescription, productId);

            sendBulkNewsletterEmails(subscribers, subject, content);

            log.info("New product notification sent to {} subscribers", subscribers.size());

        } catch (Exception e) {
            log.error("Failed to send new product notification: {}", e.getMessage());
        }
    }

    // Privat hjälpmetod för bulk-sändning
    private void sendBulkNewsletterEmails(List<CustomerEmailDTO> recipients,
                                          String subject, String content) {
        // V1.0 - Enkel implementation: skicka till alla, en i taget
        // Gmail hanterar rate limiting och spam-kontroll

        int successCount = 0;
        int failCount = 0;

        for (CustomerEmailDTO customer : recipients) {
            try {
                // Personalisera innehållet
                String personalizedContent = personalizeContent(content, customer);

                // Skicka individuellt email
                emailService.sendEmail(
                        customer.getEmail(),
                        subject,
                        personalizedContent,
                        true  // Alltid HTML för nyhetsbrev
                );

                successCount++;

                // V1.0 - Enkel rate limiting (undvik Gmail spam-filter)
                if (successCount % 50 == 0) {
                    log.info("Sent {} newsletter emails, pausing briefly...", successCount);
                    Thread.sleep(1000); // 1 sekund paus var 50:e email
                }

            } catch (Exception e) {
                failCount++;
                log.warn("Failed to send newsletter to {}: {}",
                        customer.getEmail(), e.getMessage());
                // V1.0 - Fortsätt med nästa, Gmail loggar felet
            }
        }

        log.info("Newsletter bulk send completed. Success: {}, Failed: {}",
                successCount, failCount);
    }

    private String personalizeContent(String content, CustomerEmailDTO customer) {
        // V1.0 - Enkel personalisering
        String personalized = content;

        if (customer.getFirstName() != null) {
            personalized = "Hi " + customer.getFirstName() + ",<br><br>" + content;
        } else {
            personalized = "Dear Customer,<br><br>" + content;
        }

        // Lägg till unsubscribe-länk (viktigt för spam-compliance)
        personalized += "<br><br><hr><small>You received this because you're subscribed to our newsletter. " +
                "<a href='https://perfect8.com/unsubscribe?email=" + customer.getEmail() +
                "'>Unsubscribe</a></small>";

        return personalized;
    }

    private String buildNewProductEmailContent(String productName, String description, Long productId) {
        return String.format(
                "<h2>Exciting News!</h2>" +
                        "<p>We're thrilled to introduce our latest product:</p>" +
                        "<h3>%s</h3>" +
                        "<p>%s</p>" +
                        "<p><a href='https://perfect8.com/product/%d' style='background:#007bff; color:white; " +
                        "padding:10px 20px; text-decoration:none; border-radius:5px;'>View Product</a></p>",
                productName, description, productId
        );
    }

    // V2.0 - Kommenterat för framtida segmentering
    /*
    private List<CustomerEmailDTO> getSegmentedCustomers(String segment) {
        switch(segment.toUpperCase()) {
            case "VIP":
                return shopServiceClient.getVipCustomers();
            case "NEW":
                return shopServiceClient.getNewCustomers(30); // Senaste 30 dagarna
            case "INACTIVE":
                return shopServiceClient.getInactiveCustomers(180); // 6 månader
            case "BIRTHDAY":
                return shopServiceClient.getBirthdayCustomers();
            default:
                return shopServiceClient.getNewsletterSubscribers();
        }
    }

    public void sendSegmentedCampaign(String segment, String templateId, Map<String, Object> variables) {
        // Version 2.0 - Avancerad segmentering med templates
    }

    public NewsletterAnalytics getNewsletterAnalytics(String campaignId) {
        // Version 2.0 - Tracking av öppningar, klick, etc.
    }
    */
}