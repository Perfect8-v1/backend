package com.perfect8.email.service;

import perfect8.email.dto.EmailRequestDto;
import perfect8.email.model.EmailLog;
import perfect8.email.model.EmailStatus;
import perfect8.email.repository.EmailLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private EmailRequestDto emailRequest;

    @BeforeEach
    void setUp() {
        emailRequest = new EmailRequestDto();
        emailRequest.setTo("test@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setBody("Test Body");
    }

    @Test
    void sendEmail_SimpleEmail_Success() throws Exception {
        // Arrange
        EmailLog savedLog = new EmailLog();
        savedLog.setId(1L);
        savedLog.setStatus(EmailStatus.SENT);

        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(savedLog);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        CompletableFuture<EmailLog> result = emailService.sendEmail(emailRequest);
        EmailLog emailLog = result.get();

        // Assert
        assertNotNull(emailLog);
        assertEquals(EmailStatus.SENT, emailLog.getStatus());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(emailLogRepository, times(1)).save(any(EmailLog.class));
    }

    @Test
    void sendSimpleEmail_Success() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendSimpleEmail("test@example.com", "Subject", "Body");

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}