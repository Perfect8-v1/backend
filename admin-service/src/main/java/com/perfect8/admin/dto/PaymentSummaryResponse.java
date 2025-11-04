package com.perfect8.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentSummaryResponse {
    private BigDecimal totalPayments;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    private Long totalTransactions;
    private List<PaymentDetailResponse> recentPayments;

    // Constructors
    public PaymentSummaryResponse() {}

    // Getters and Setters
    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }

    public BigDecimal getTotalRefunds() { return totalRefunds; }
    public void setTotalRefunds(BigDecimal totalRefunds) { this.totalRefunds = totalRefunds; }

    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }

    public Long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }

    public List<PaymentDetailResponse> getRecentPayments() { return recentPayments; }
    public void setRecentPayments(List<PaymentDetailResponse> recentPayments) { this.recentPayments = recentPayments; }
}