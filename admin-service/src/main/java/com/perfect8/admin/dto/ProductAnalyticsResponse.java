package com.perfect8.admin.dto;

public class ProductAnalyticsResponse {
    private String productAnalytics;

    public ProductAnalyticsResponse() {}
    public ProductAnalyticsResponse(String productAnalytics) { this.productAnalytics = productAnalytics; }

    public String getProductAnalytics() { return productAnalytics; }
    public void setProductAnalytics(String productAnalytics) { this.productAnalytics = productAnalytics; }
}
