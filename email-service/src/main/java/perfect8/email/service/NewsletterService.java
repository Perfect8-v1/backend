package perfect8.email.service;

import perfect8.email.dto.NewsletterRequestDto;
import perfect8.email.model.EmailLog;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class NewsletterService {

    private final EmailService emailService;

    public NewsletterService(EmailService emailService) {
        this.emailService = emailService;
    }

    public List<CompletableFuture<EmailLog>> sendNewsletter(NewsletterRequestDto request) {
        List<CompletableFuture<EmailLog>> futures = new ArrayList<>();

        for (String recipient : request.getRecipients()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("subject", request.getSubject());
            variables.put("content", request.getContent());
            variables.put("blogPostUrl", request.getBlogPostUrl());
            variables.put("unsubscribeUrl", generateUnsubscribeUrl(recipient));

            CompletableFuture<EmailLog> future = emailService.sendTemplateEmail(
                    recipient,
                    "newsletter",
                    variables
            );
            futures.add(future);
        }

        return futures;
    }

    private String generateUnsubscribeUrl(String email) {
        //TODO put in the correct domainaddress ******************************************************

        // Implement unsubscribe URL generation logic
        return "https://perfect8blog.com/unsubscribe?email=" + email;
    }
}
