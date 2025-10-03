package com.perfect8.admin.dto;

public class OrderAttentionResponse {
    private String orderAttention;

    public OrderAttentionResponse() {}
    public OrderAttentionResponse(String orderAttention) { this.orderAttention = orderAttention; }

    public String getOrderAttention() { return orderAttention; }
    public void setOrderAttention(String orderAttention) { this.orderAttention = orderAttention; }
}
