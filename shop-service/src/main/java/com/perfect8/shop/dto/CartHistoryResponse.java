package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartHistoryResponse {

    private Long cartHistoryResponseId;
    private Long customerId;
    private String customerEmail;
    private String sessionId;
    private String action;
    private String actionType;
    private LocalDateTime timestamp;
    private String description;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private Integer quantityChanged;
    private BigDecimal priceBefore;
    private BigDecimal priceAfter;
    private BigDecimal totalBefore;
    private BigDecimal totalAfter;
    private String reason;
    private String source;
    private String userAgent;
    private String ipAddress;
    private String deviceType;
    private Boolean isAutomated;
    private String automationRule;
    private List<CartItemSnapshot> itemSnapshots;
    private String metadata;
    private Boolean isReversible;
    private Long reversalId;
    private String cartStatus;
    private Integer totalItemsBefore;
    private Integer totalItemsAfter;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemSnapshot {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String action;
    }
}