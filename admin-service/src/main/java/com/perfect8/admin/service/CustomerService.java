package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    public String getCustomers() {
        return "Customers data";
    }

    public String getCustomerById(Long id) {
        return "Customer " + id;
    }
}
