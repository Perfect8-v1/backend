package perfect8.email.controller;

import perfect8.email.dto.EmailRequestDto;
import perfect8.email.dto.EmailResponseDto;
import perfect8.email.dto.NewsletterRequestDto;
import perfect8.email.model.EmailLog;
import perfect8.email.model.EmailStatus;
import perfect8.email.service.EmailService;
import perfect8.email.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final NewsletterService newsletterService;

    public EmailController(EmailService emailService, NewsletterService newsletterService) {
        this.emailService = emailService;
        this.newsletterService = newsletterService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        CompletableFuture<EmailLog> future = emailService.sendEmail(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new EmailResponseDto(null, "Email queued for sending", EmailStatus.PENDING));
    }

    @PostMapping("/newsletter")
    public ResponseEntity<String> sendNewsletter(@Valid @RequestBody NewsletterRequestDto request) {
        List<CompletableFuture<EmailLog>> futures = newsletterService.sendNewsletter(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Newsletter queued for " + futures.size() + " recipients");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }
}
