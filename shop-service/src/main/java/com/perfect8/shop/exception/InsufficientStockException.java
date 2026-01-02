package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    private Long productId;
    private String productName;
    private String productSku;
    private Integer requestedQuantity;
    private Integer availableQuantity;

    // Default constructor
    public InsufficientStockException() {
        super();
    }

    // Constructor with message
    public InsufficientStockException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with stock details
    public InsufficientStockException(Long productId, String productName,
                                      Integer requestedQuantity, Integer availableQuantity) {
        super(formatMessage(productName, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Constructor with full product details
    public InsufficientStockException(Long productId, String productName, String productSku,
                                      Integer requestedQuantity, Integer availableQuantity) {
        super(formatMessage(productName, productSku, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Constructor with custom message and stock details
    public InsufficientStockException(String message, Long productId, String productName,
                                      Integer requestedQuantity, Integer availableQuantity) {
        super(message);
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Static factory methods
    public static InsufficientStockException forProduct(Long productId, String productName,
                                                        Integer requested, Integer available) {
        return new InsufficientStockException(productId, productName, requested, available);
    }

    public static InsufficientStockException forProductSku(Long productId, String productName, String sku,
                                                           Integer requested, Integer available) {
        return new InsufficientStockException(productId, productName, sku, requested, available);
    }

    public static InsufficientStockException outOfStock(String productName) {
        return new InsufficientStockException(
                String.format("Product '%s' is out of stock", productName)
        );
    }

    public static InsufficientStockException outOfStock(Long productId, String productName) {
        return new InsufficientStockException(productId, productName, 1, 0);
    }

    public static InsufficientStockException withMessage(String message) {
        return new InsufficientStockException(message);
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    // Utility methods
    public boolean hasProductDetails() {
        return productId != null && productName != null;
    }

    public boolean hasStockDetails() {
        return requestedQuantity != null && availableQuantity != null;
    }

    public boolean isCompletelyOutOfStock() {
        return availableQuantity != null && availableQuantity == 0;
    }

    public Integer getShortfall() {
        if (requestedQuantity != null && availableQuantity != null) {
            return requestedQuantity - availableQuantity;
        }
        return null;
    }

    public String getProductDisplay() {
        if (productName != null && productSku != null) {
            return String.format("%s (%s)", productName, productSku);
        } else if (productName != null) {
            return productName;
        } else if (productSku != null) {
            return productSku;
        } else if (productId != null) {
            return "Product ID: " + productId;
        }
        return "Unknown Product";
    }

    public String getUserFriendlyMessage() {
        if (isCompletelyOutOfStock()) {
            return String.format("Sorry, %s is currently out of stock.", getProductDisplay());
        } else if (hasStockDetails()) {
            return String.format(
                    "Sorry, only %d units of %s are available, but you requested %d.",
                    availableQuantity,
                    getProductDisplay(),
                    requestedQuantity
            );
        } else {
            return String.format("Insufficient stock for %s.", getProductDisplay());
        }
    }

    public String getDetailedMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Insufficient stock");

        if (hasProductDetails()) {
            message.append(" for product: ").append(getProductDisplay());
        }

        if (hasStockDetails()) {
            message.append(String.format(
                    " (Requested: %d, Available: %d, Shortfall: %d)",
                    requestedQuantity,
                    availableQuantity,
                    getShortfall()
            ));
        }

        return message.toString();
    }

    // Helper methods for message formatting
    private static String formatMessage(String productName, Integer requested, Integer available) {
        if (available == 0) {
            return String.format("Product '%s' is out of stock", productName);
        }
        return String.format(
                "Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requested, available
        );
    }

    private static String formatMessage(String productName, String sku, Integer requested, Integer available) {
        if (available == 0) {
            return String.format("Product '%s' (%s) is out of stock", productName, sku);
        }
        return String.format(
                "Insufficient stock for product '%s' (%s). Requested: %d, Available: %d",
                productName, sku, requested, available
        );
    }

    @Override
    public String toString() {
        return "InsufficientStockException{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productSku='" + productSku + '\'' +
                ", requestedQuantity=" + requestedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}