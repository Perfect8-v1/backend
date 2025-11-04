package com.perfect8.admin.dto;

public class NewCustomerResponse {
    private String newCustomer;

    public NewCustomerResponse() {}
    public NewCustomerResponse(String newCustomer) { this.newCustomer = newCustomer; }

    public String getNewCustomer() { return newCustomer; }
    public void setNewCustomer(String newCustomer) { this.newCustomer = newCustomer; }
}
