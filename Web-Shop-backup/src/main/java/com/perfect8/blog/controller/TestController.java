package main.java.com.perfect8.blog.controller;

import main.java.com.perfect8.blog.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a temporary controller for testing purposes.
 * We can delete it later.
 */
@RestController
public class TestController {

    private final EmailService emailService;

    @Autowired
    public TestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Creates an endpoint at http://localhost:8080/send-test-email
     * that will trigger our email service.
     */
    @GetMapping("/send-test-email")
    public String sendTestEmail() {
        try {
            // IMPORTANT: Change this email address to your own to receive the test.
            String recipientEmail = "jonteyh@gmail.com";

            emailService.sendSimpleEmail(
                    recipientEmail,
                    "Test Email from Spring Boot",
                    "Congratulations! Your email configuration is working correctly."
            );

            return "Email sent successfully to " + recipientEmail;

        } catch (Exception e) {
            // Print the error to the console for debugging
            e.printStackTrace();
            return "Failed to send email. Check the console for errors. Reason: " + e.getMessage();
        }
    }
}
