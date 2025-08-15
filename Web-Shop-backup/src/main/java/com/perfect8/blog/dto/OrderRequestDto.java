package main.java.com.perfect8.blog.dto;


import java.util.List;

public class OrderRequestDto {
    // We will get the customerId from the logged-in user's security context,
    // so we don't need it here.

    private List<OrderItemDto> items;
    private String shippingAddress;
    private String billingAddress;

    // Generate Getters and Setters
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
}