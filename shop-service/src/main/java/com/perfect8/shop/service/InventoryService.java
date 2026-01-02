package com.perfect8.shop.service;

import com.perfect8.shop.entity.Product;
import com.perfect8.shop.entity.InventoryTransaction;
import com.perfect8.shop.repository.ProductRepository;
import com.perfect8.shop.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Service - Version 1.0
 * Core inventory functionality for order processing
 *
 * CRITICAL: Stock accuracy is essential for customer satisfaction!
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    /**
     * Check if product is available for order
     * Used by OrderService during order creation
     */
    public boolean checkAvailability(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.warn("Product {} not found when checking availability", productId);
                return false;
            }

            boolean available = product.getStockQuantity() >= quantity;
            if (!available) {
                log.info("Product {} insufficient stock: requested {}, available {}",
                        productId, quantity, product.getStockQuantity());
            }
            return available;
        } catch (Exception e) {
            log.error("Error checking availability for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock(Long productId, Integer quantity) {
        return checkAvailability(productId, quantity);
    }

    /**
     * Get current stock level
     */
    public Integer getStockLevel(Long productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            return product != null ? product.getStockQuantity() : 0;
        } catch (Exception e) {
            log.error("Error getting stock level for product {}: {}", productId, e.getMessage());
            return 0;
        }
    }

    /**
     * Reserve stock for pending order
     * Used when order is created but payment not yet confirmed
     */
    @Transactional
    public boolean reserveStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("Product {} not found for stock reservation", productId);
                return false;
            }

            if (product.getStockQuantity() < quantity) {
                log.warn("Insufficient stock for product {}: requested {}, available {}",
                        productId, quantity, product.getStockQuantity());
                return false;
            }

            // Decrease stock
            Integer oldQuantity = product.getStockQuantity();
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            // Log transaction
            logInventoryTransaction(product, "RESERVED", oldQuantity,
                    product.getStockQuantity(), -quantity, "Stock reserved for order");

            log.info("Reserved {} units of product {}", quantity, productId);
            return true;

        } catch (Exception e) {
            log.error("Error reserving stock for product {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Release reserved stock (for cancelled/failed orders)
     * Used when order is cancelled before payment
     */
    @Transactional
    public boolean releaseReservedStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("Product {} not found for stock release", productId);
                return false;
            }

            // Increase stock back
            Integer oldQuantity = product.getStockQuantity();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);

            // Log transaction
            logInventoryTransaction(product, "RELEASED", oldQuantity,
                    product.getStockQuantity(), quantity, "Reserved stock released");

            log.info("Released {} reserved units of product {}", quantity, productId);
            return true;

        } catch (Exception e) {
            log.error("Error releasing reserved stock for product {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Confirm stock allocation when payment is successful
     * Converts reserved stock to sold
     */
    @Transactional
    public boolean confirmStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("Product {} not found for stock confirmation", productId);
                return false;
            }

            // Stock already decreased during reservation
            // Just log the confirmation
            logInventoryTransaction(product, "STOCK_OUT", product.getStockQuantity(),
                    product.getStockQuantity(), 0, "Stock confirmed after payment");

            log.info("Confirmed {} units sold for product {}", quantity, productId);
            return true;

        } catch (Exception e) {
            log.error("Error confirming stock for product {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Release stock (generic method for backwards compatibility)
     */
    @Transactional
    public boolean releaseStock(Long productId, Integer quantity) {
        return releaseReservedStock(productId, quantity);
    }

    /**
     * Return stock to inventory (for returns)
     */
    @Transactional
    public boolean returnToStock(Long productId, Integer quantity) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("Product {} not found for return to stock", productId);
                return false;
            }

            // Increase stock for returned items
            Integer oldQuantity = product.getStockQuantity();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);

            // Log transaction
            logInventoryTransaction(product, "STOCK_IN", oldQuantity,
                    product.getStockQuantity(), quantity, "Items returned to stock");

            log.info("Returned {} units to stock for product {}", quantity, productId);
            return true;

        } catch (Exception e) {
            log.error("Error returning stock for product {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Adjust stock with reason
     */
    @Transactional
    public boolean adjustStock(Long productId, Integer adjustment, String reason) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("Product {} not found for stock adjustment", productId);
                return false;
            }

            Integer oldQuantity = product.getStockQuantity();
            int newStock = product.getStockQuantity() + adjustment;
            if (newStock < 0) {
                log.warn("Cannot adjust stock below 0 for product {}", productId);
                return false;
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);

            // Log transaction
            String type = adjustment > 0 ? "ADJUSTMENT" : "ADJUSTMENT";
            logInventoryTransaction(product, type, oldQuantity,
                    newStock, adjustment, reason);

            log.info("Adjusted stock for product {} by {} units. Reason: {}",
                    productId, adjustment, reason);
            return true;

        } catch (Exception e) {
            log.error("Error adjusting stock for product {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Log inventory transaction for audit trail
     */
    private void logInventoryTransaction(Product product, String transactionType,
                                         Integer quantityBefore, Integer quantityAfter,
                                         Integer quantityChange, String notes) {
        try {
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .product(product)  // FIXED: Using product entity instead of productId
                    .transactionType(transactionType)
                    .quantityBefore(quantityBefore)
                    .quantityAfter(quantityAfter)
                    .quantityChange(quantityChange)
                    .transactionDate(LocalDateTime.now())
                    .reason(notes)
                    .userId("SYSTEM") // In v2.0, get from security context
                    .build();

            inventoryTransactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Failed to log inventory transaction: {}", e.getMessage());
            // Don't fail the main operation if logging fails
        }
    }

    /**
     * Check if product is low on stock
     */
    public boolean isLowStock(Long productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return false;
            }

            // Check against reorder point if set
            if (product.getReorderPoint() != null && product.getReorderPoint() > 0) {
                return product.getStockQuantity() <= product.getReorderPoint();
            }

            // Default low stock threshold
            return product.getStockQuantity() <= 10;

        } catch (Exception e) {
            log.error("Error checking low stock for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Get products that are low on stock
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        try {
            if (threshold == null) {
                threshold = 10;
            }

            // Simple implementation for v1.0
            final Integer finalThreshold = threshold;
            return productRepository.findAll().stream()
                    .filter(p -> p.getStockQuantity() <= finalThreshold)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting low stock products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if inventory service is enabled
     */
    public boolean isInventoryTrackingEnabled() {
        return true; // Always enabled in v1.0
    }

    /**
     * Get inventory status
     */
    public String getInventoryStatus() {
        return "Core inventory tracking active (v1.0)";
    }

    // ========== VERSION 2.0 STUB METHODS ==========
    // These return minimal data for v2.0 features

    /**
     * V1.0 STUB - Returns basic inventory metrics
     */
    public Map<String, Object> getInventoryMetrics() {
        log.debug("InventoryService.getInventoryMetrics() - minimal v1.0 implementation");

        Map<String, Object> basicMetrics = new HashMap<>();
        basicMetrics.put("totalProducts", productRepository.count());
        basicMetrics.put("totalValue", BigDecimal.ZERO);
        basicMetrics.put("lowStockItems", getLowStockProducts(10).size());

        // Count out of stock items manually for v1.0
        long outOfStock = productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() <= 0)
                .count();
        basicMetrics.put("outOfStockItems", outOfStock);
        basicMetrics.put("version", "1.0-basic");

        return basicMetrics;
    }

    /**
     * V1.0 STUB - Returns empty inventory report
     */
    public Map<String, Object> getInventoryReport(String period) {
        log.debug("InventoryService.getInventoryReport() - not implemented in v1.0");

        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("period", period);
        emptyData.put("message", "Detailed reports available in v2.0");

        return emptyData;
    }

    /**
     * V1.0 STUB - Returns empty low stock alerts
     */
    public List<Map<String, Object>> getLowStockAlerts(Integer threshold) {
        log.debug("InventoryService.getLowStockAlerts() - simplified v1.0 implementation");

        List<Map<String, Object>> alerts = new ArrayList<>();
        List<Product> lowStock = getLowStockProducts(threshold);

        for (Product product : lowStock) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("productId", product.getProductId());
            alert.put("productName", product.getName());
            alert.put("currentStock", product.getStockQuantity());
            alerts.add(alert);
        }

        return alerts;
    }

    /**
     * V1.0 STUB - Returns basic stock adjustment history
     */
    public List<Map<String, Object>> getStockAdjustmentHistory(Long productId) {
        log.debug("InventoryService.getStockAdjustmentHistory() - minimal v1.0 implementation");

        // Simplified for v1.0 - return empty list
        // Full transaction history will be implemented in v2.0
        return new ArrayList<>();
    }

    /**
     * V1.0 STUB - Returns empty reorder suggestions
     */
    public List<Map<String, Object>> getReorderSuggestions() {
        log.debug("InventoryService.getReorderSuggestions() - not implemented in v1.0");
        return new ArrayList<>();
    }
}