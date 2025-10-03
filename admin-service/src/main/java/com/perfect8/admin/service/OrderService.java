package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String getOrders() {
        return "Orders data";
    }

    public String getOrderById(Long id) {
        return "Order " + id;
    }
}
