package com.perfect8.shop.enums;

/**
 * Enumeration for different types of inventory transactions
 * Used to categorize and track various inventory movements
 */
public enum TransactionType {

    /**
     * Stock increase - new inventory added to system
     */
    STOCK_IN("Stock In", "Inventory added to stock"),

    /**
     * Stock decrease - inventory removed from system
     */
    STOCK_OUT("Stock Out", "Inventory removed from stock"),

    /**
     * Sale transaction - product sold to customer
     */
    SALE("Sale", "Product sold to customer"),

    /**
     * Return transaction - product returned by customer
     */
    RETURN("Return", "Product returned by customer"),

    /**
     * Purchase from supplier
     */
    PURCHASE("Purchase", "Product purchased from supplier"),

    /**
     * Adjustment for inventory corrections
     */
    ADJUSTMENT("Adjustment", "Inventory level correction"),

    /**
     * Damaged goods removal
     */
    DAMAGE("Damage", "Product removed due to damage"),

    /**
     * Expired goods removal
     */
    EXPIRED("Expired", "Product removed due to expiration"),

    /**
     * Transfer between locations/warehouses
     */
    TRANSFER_OUT("Transfer Out", "Product transferred out"),

    /**
     * Transfer between locations/warehouses
     */
    TRANSFER_IN("Transfer In", "Product transferred in"),

    /**
     * Reserved for pending orders
     */
    RESERVATION("Reservation", "Product reserved for order"),

    /**
     * Unreserved - cancelled reservation
     */
    UNRESERVATION("Unreservation", "Product reservation cancelled"),

    /**
     * Initial stock when setting up system
     */
    INITIAL_STOCK("Initial Stock", "Initial inventory setup"),

    /**
     * Promotion or sample giveaway
     */
    PROMOTION("Promotion", "Product used for promotion/sample"),

    /**
     * Theft or loss
     */
    LOSS("Loss", "Product lost or stolen"),

    /**
     * Quality control rejection
     */
    QUALITY_REJECT("Quality Reject", "Product rejected in quality control"),

    /**
     * Restock from return processing
     */
    RESTOCK("Restock", "Product returned to available stock"),

    /**
     * Manufacturing or assembly
     */
    MANUFACTURING("Manufacturing", "Product created through manufacturing"),

    /**
     * Disassembly of product bundles
     */
    DISASSEMBLY("Disassembly", "Product bundle disassembled");

    private final String displayName;
    private final String description;

    /**
     * Constructor for TransactionType enum
     *
     * @param displayName User-friendly display name
     * @param description Detailed description of the transaction type
     */
    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the user-friendly display name
     *
     * @return Display name for UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the detailed description
     *
     * @return Description of the transaction type
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this transaction type increases stock
     *
     * @return true if transaction increases stock levels
     */
    public boolean increasesStock() {
        return switch (this) {
            case STOCK_IN, RETURN, PURCHASE, TRANSFER_IN,
                 UNRESERVATION, INITIAL_STOCK, RESTOCK,
                 MANUFACTURING -> true;
            default -> false;
        };
    }

    /**
     * Check if this transaction type decreases stock
     *
     * @return true if transaction decreases stock levels
     */
    public boolean decreasesStock() {
        return switch (this) {
            case STOCK_OUT, SALE, DAMAGE, EXPIRED, TRANSFER_OUT,
                 RESERVATION, PROMOTION, LOSS, QUALITY_REJECT,
                 DISASSEMBLY -> true;
            default -> false;
        };
    }

    /**
     * Check if this transaction type is neutral (no stock change)
     *
     * @return true if transaction doesn't change stock levels
     */
    public boolean isNeutral() {
        return this == ADJUSTMENT;
    }

    /**
     * Check if this transaction type is customer-facing
     *
     * @return true if transaction involves customers
     */
    public boolean isCustomerFacing() {
        return switch (this) {
            case SALE, RETURN, PROMOTION -> true;
            default -> false;
        };
    }

    /**
     * Check if this transaction type is supplier-related
     *
     * @return true if transaction involves suppliers
     */
    public boolean isSupplierRelated() {
        return this == PURCHASE;
    }

    /**
     * Check if this transaction type is internal operation
     *
     * @return true if transaction is internal operation
     */
    public boolean isInternalOperation() {
        return switch (this) {
            case ADJUSTMENT, DAMAGE, EXPIRED, TRANSFER_OUT, TRANSFER_IN,
                 RESERVATION, UNRESERVATION, INITIAL_STOCK, LOSS,
                 QUALITY_REJECT, RESTOCK, MANUFACTURING, DISASSEMBLY -> true;
            default -> false;
        };
    }

    /**
     * Get all transaction types that increase stock
     *
     * @return Array of transaction types that increase stock
     */
    public static TransactionType[] getStockIncreasingTypes() {
        return new TransactionType[]{
                STOCK_IN, RETURN, PURCHASE, TRANSFER_IN,
                UNRESERVATION, INITIAL_STOCK, RESTOCK, MANUFACTURING
        };
    }

    /**
     * Get all transaction types that decrease stock
     *
     * @return Array of transaction types that decrease stock
     */
    public static TransactionType[] getStockDecreasingTypes() {
        return new TransactionType[]{
                STOCK_OUT, SALE, DAMAGE, EXPIRED, TRANSFER_OUT,
                RESERVATION, PROMOTION, LOSS, QUALITY_REJECT, DISASSEMBLY
        };
    }

    /**
     * Get all customer-facing transaction types
     *
     * @return Array of customer-facing transaction types
     */
    public static TransactionType[] getCustomerFacingTypes() {
        return new TransactionType[]{SALE, RETURN, PROMOTION};
    }

    /**
     * Parse transaction type from string (case-insensitive)
     *
     * @param value String value to parse
     * @return TransactionType enum value
     * @throws IllegalArgumentException if value cannot be parsed
     */
    public static TransactionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TransactionType value cannot be null or empty");
        }

        String normalizedValue = value.trim().toUpperCase().replace(" ", "_");

        try {
            return TransactionType.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (TransactionType type : TransactionType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown TransactionType: " + value);
        }
    }

    /**
     * Get transaction type for order fulfillment
     *
     * @return SALE transaction type
     */
    public static TransactionType forOrderFulfillment() {
        return SALE;
    }

    /**
     * Get transaction type for order cancellation
     *
     * @return UNRESERVATION transaction type
     */
    public static TransactionType forOrderCancellation() {
        return UNRESERVATION;
    }

    /**
     * Get transaction type for order reservation
     *
     * @return RESERVATION transaction type
     */
    public static TransactionType forOrderReservation() {
        return RESERVATION;
    }

    @Override
    public String toString() {
        return displayName;
    }
}