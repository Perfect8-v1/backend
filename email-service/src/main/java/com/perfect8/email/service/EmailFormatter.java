package com.perfect8.email.service;

import com.perfect8.email.model.EmailMessage;
import com.perfect8.email.dto.OrderEmailDTO;
import com.perfect8.email.dto.CustomerEmailDTO;
import com.perfect8.common.enums.OrderStatus;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public interface EmailFormatter {

    /**
     * Formats an EmailMessage into final email content
     * @param message The email message object containing all data
     * @return Formatted email content ready to send
     */
    FormattedEmail format(EmailMessage message);

    /**
     * Container for formatted email content
     */
    class FormattedEmail {
        private final String subject;
        private final String plainTextContent;
        private final String htmlContent;
        private final boolean isHtml;

        public FormattedEmail(String subject, String content, boolean isHtml) {
            this.subject = subject;
            this.isHtml = isHtml;
            if (isHtml) {
                this.htmlContent = content;
                this.plainTextContent = stripHtml(content);
            } else {
                this.plainTextContent = content;
                this.htmlContent = convertToHtml(content);
            }
        }

        public String getSubject() { return subject; }
        public String getPlainTextContent() { return plainTextContent; }
        public String getHtmlContent() { return htmlContent; }
        public boolean isHtml() { return isHtml; }

        private String stripHtml(String html) {
            // Simple HTML stripping for fallback
            return html.replaceAll("<[^>]+>", "")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .trim();
        }

        private String convertToHtml(String plainText) {
            // Simple plain text to HTML conversion
            return "<html><body><pre>" +
                    plainText.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\n", "<br>") +
                    "</pre></body></html>";
        }
    }

    /**
     * Default implementation using simple string templates
     * Can be replaced with Thymeleaf, Freemarker, Velocity, etc.
     */
    class SimpleEmailFormatter implements EmailFormatter {

        private static final DateTimeFormatter DATE_FORMAT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public FormattedEmail format(EmailMessage message) {
            String messageType = message.getMessageType();
            Object content = message.getContentObject();

            // Route to specific formatters based on message type
            if ("ORDER_CONFIRMATION".equals(messageType) && content instanceof OrderEmailDTO) {
                return formatOrderConfirmation((OrderEmailDTO) content);
            } else if ("SHIPPING_NOTIFICATION".equals(messageType) && content instanceof OrderEmailDTO) {
                return formatShippingNotification((OrderEmailDTO) content);
            } else if ("WELCOME_EMAIL".equals(messageType) && content instanceof CustomerEmailDTO) {
                return formatWelcomeEmail((CustomerEmailDTO) content);
            } else {
                // Default formatting using template variables
                return formatWithTemplate(message);
            }
        }

        private FormattedEmail formatOrderConfirmation(OrderEmailDTO order) {
            String subject = "Orderbekräftelse #" + order.getOrderNumber();

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><body>");
            html.append("<h2>Tack för din beställning!</h2>");
            html.append("<p>Hej ").append(order.getCustomerName()).append(",</p>");
            html.append("<p>Vi har mottagit din order #").append(order.getOrderNumber()).append("</p>");
            html.append("<h3>Orderdetaljer:</h3>");
            html.append("<table border='1' style='border-collapse: collapse;'>");
            html.append("<tr><th>Produkt</th><th>Antal</th><th>Pris</th></tr>");

            order.getItems().forEach(item -> {
                html.append("<tr>");
                html.append("<td>").append(item.getProductName()).append("</td>");
                html.append("<td>").append(item.getQuantity()).append("</td>");
                html.append("<td>").append(String.format("%.2f kr", item.getPrice())).append("</td>");
                html.append("</tr>");
            });

            html.append("</table>");
            html.append("<p><strong>Total: ").append(String.format("%.2f kr", order.getTotalAmount())).append("</strong></p>");
            html.append("<p>Leveransadress:<br>");
            html.append(order.getShippingAddress()).append("</p>");
            html.append("<p>Med vänliga hälsningar,<br>Perfect8 Team</p>");
            html.append("</body></html>");

            return new FormattedEmail(subject, html.toString(), true);
        }

        private FormattedEmail formatShippingNotification(OrderEmailDTO order) {
            String subject = "Din order #" + order.getOrderNumber() + " har skickats!";

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><body>");
            html.append("<h2>Din order är på väg!</h2>");
            html.append("<p>Hej ").append(order.getCustomerName()).append(",</p>");
            html.append("<p>Goda nyheter! Din order #").append(order.getOrderNumber());
            html.append(" har skickats och är på väg till dig.</p>");

            if (order.getTrackingNumber() != null) {
                html.append("<p>Spårningsnummer: <strong>").append(order.getTrackingNumber()).append("</strong></p>");
            }

            html.append("<p>Beräknad leverans: 2-5 arbetsdagar</p>");
            html.append("<p>Med vänliga hälsningar,<br>Perfect8 Team</p>");
            html.append("</body></html>");

            return new FormattedEmail(subject, html.toString(), true);
        }

        private FormattedEmail formatWelcomeEmail(CustomerEmailDTO customer) {
            String subject = "Välkommen till Perfect8, " + customer.getFirstName() + "!";

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><body>");
            html.append("<h1>Välkommen till Perfect8!</h1>");
            html.append("<p>Hej ").append(customer.getFirstName()).append(",</p>");
            html.append("<p>Vi är glada att ha dig som kund hos oss!</p>");
            html.append("<p>Som ny kund får du 10% rabatt på din första order.</p>");
            html.append("<p>Använd koden: <strong>WELCOME10</strong></p>");
            html.append("<p>Med vänliga hälsningar,<br>Perfect8 Team</p>");
            html.append("</body></html>");

            return new FormattedEmail(subject, html.toString(), true);
        }

        private FormattedEmail formatWithTemplate(EmailMessage message) {
            Map<String, Object> variables = message.getTemplateVariables();
            String subject = message.getSubject();

            if (subject == null) {
                subject = "Meddelande från Perfect8";
            }

            // Simple template replacement
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = "{{" + entry.getKey() + "}}";
                String value = String.valueOf(entry.getValue());
                subject = subject.replace(key, value);
            }

            // Build simple HTML body from variables
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><body>");
            html.append("<div style='font-family: Arial, sans-serif;'>");

            if (variables.containsKey("content")) {
                html.append(variables.get("content"));
            } else {
                // Auto-generate content from variables
                variables.forEach((key, value) -> {
                    if (!"subject".equals(key)) {
                        html.append("<p><strong>").append(key).append(":</strong> ");
                        html.append(value).append("</p>");
                    }
                });
            }

            html.append("</div></body></html>");

            return new FormattedEmail(subject, html.toString(), true);
        }
    }
}