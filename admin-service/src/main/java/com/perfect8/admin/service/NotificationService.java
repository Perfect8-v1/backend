package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendNotification(String message) {
        System.out.println("Notification sent: " + message);
    }

    public void sendEmail(String to, String subject, String body) {
        System.out.println("Email sent to " + to + ": " + subject);
    }
}