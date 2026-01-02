package com.perfect8.shop.exception;

public class DuplicateSkuException extends RuntimeException {

    // Konstruktor för generella meddelanden
    public DuplicateSkuException(String message) {
        super(message);
    }

    // Konstruktor med cause
    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }

    // Statisk fabriksmetod för SKU-specifika fel (för att undvika förvirring)
    public static DuplicateSkuException forSku(String sku) {
        return new DuplicateSkuException("Product with SKU '" + sku + "' already exists");
    }
}