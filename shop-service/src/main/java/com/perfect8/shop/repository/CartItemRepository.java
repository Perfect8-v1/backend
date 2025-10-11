package com.perfect8.shop.repository;

import com.perfect8.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CartItemRepository - Version 1.0
 * FIXED: Changed expiresAt to expirationDate to match Cart entity field name
 * Magnum Opus Compliant: SAMMA namn överallt
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = :productId")
    List<CartItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId")
    List<CartItem> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId AND ci.isSavedForLater = false")
    List<CartItem> findActiveCartItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId AND ci.isSavedForLater = true")
    List<CartItem> findSavedCartItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.stockAvailable = false")
    List<CartItem> findUnavailableItems();

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.isGift = true")
    List<CartItem> findSelectedItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.isGift = false")
    List<CartItem> findUnselectedItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId")
    Integer countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId AND ci.isSavedForLater = false")
    Integer sumQuantityByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(ci.subtotal) FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId AND ci.isSavedForLater = false")
    Double sumTotalPriceByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.isGift = true AND ci.cart.customer.customerId = :customerId")
    List<CartItem> findGiftItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.addedAt < :cutoffDate AND ci.isSavedForLater = false")
    List<CartItem> findOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT DISTINCT ci.product.category.name FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId")
    List<String> findDistinctCategoriesByCustomerId(@Param("customerId") Long customerId);

    /* VERSION 2.0 - Product brand field not implemented in v1.0
    @Query("SELECT DISTINCT ci.product.brand FROM CartItem ci WHERE ci.cart.customer.customerId = :customerId")
    List<String> findDistinctBrandsByCustomerId(@Param("customerId") Long customerId);
    */

    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    @Query("DELETE FROM CartItem ci WHERE ci.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    // FIXED: Changed c.expiresAt to c.expirationDate to match Cart entity field name
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId IN (SELECT c.cartId FROM Cart c WHERE c.expirationDate < :expiryDate)")
    void deleteByExpiredCart(@Param("expiryDate") LocalDateTime expiryDate);
}