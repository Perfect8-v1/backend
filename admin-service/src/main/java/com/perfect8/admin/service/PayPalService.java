package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class PayPalService {

    public String createPayment() {
        return "PayPal payment created";
    }

    public String executePayment(String paymentId, String payerId) {
        return "PayPal payment executed: " + paymentId;
    }
}
