package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public String getPayments() {
        return "Payments data";
    }

    public String processPayment(String paymentId) {
        return "Payment processed: " + paymentId;
    }
}
