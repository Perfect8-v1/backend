package perfect8.email.service;

import perfect8.email.dto.BulkEmailRequestDto;
import perfect8.email.dto.EmailRequestDto;
import perfect8.email.dto.EmailResponseDto;
import perfect8.email.dto.EmailStatusDto;

import java.util.List;

public interface EmailService {
    EmailResponseDto sendEmail(EmailRequestDto request);
    EmailResponseDto sendOrderConfirmation(Long orderId, String customerEmail);
    EmailResponseDto sendShippingNotification(Long orderId, String trackingNumber, String customerEmail);
    EmailResponseDto sendPasswordReset(String email, String resetToken);
    List<EmailResponseDto> sendBulkEmails(BulkEmailRequestDto request);
    EmailStatusDto getEmailStatus(String emailId);
    List<String> getAvailableTemplates();
    EmailResponseDto sendTestEmail(String recipientEmail);
}
