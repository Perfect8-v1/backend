package com.perfect8.shop.repository;

import com.perfect8.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByProductId(Long productId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    List<CartItem> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.id = :customerId AND ci.cart.saved = false")
    List<CartItem> findActiveCartItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.customer.id = :customerId AND ci.cart.saved = true")
    List<CartItem> findSavedCartItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.available = false")
    List<CartItem> findUnavailableItems();

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.selected = true")
    List<CartItem> findSelectedItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.selected = false")
    List<CartItem> findUnselectedItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    Integer countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.customer.id = :customerId AND ci.cart.saved = false")
    Integer sumQuantityByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(ci.totalPrice) FROM CartItem ci WHERE ci.cart.customer.id = :customerId AND ci.cart.saved = false")
    Double sumTotalPriceByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.isGift = true AND ci.cart.customer.id = :customerId")
    List<CartItem> findGiftItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.addedAt < :cutoffDate AND ci.cart.saved = false")
    List<CartItem> findOldCartItems(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT DISTINCT ci.productCategory FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    List<String> findDistinctCategoriesByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT ci.productBrand FROM CartItem ci WHERE ci.cart.customer.id = :customerId")
    List<String> findDistinctBrandsByCustomerId(@Param("customerId") Long customerId);

    void deleteByCartId(Long cartId);

    void deleteByProductId(Long productId);

    @Query("DELETE FROM CartItem ci WHERE ci.cart.id IN (SELECT c.id FROM Cart c WHERE c.expiresAt < :expiryDate)")
    void deleteByExpiredCart(@Param("expiryDate") LocalDateTime expiryDate);
}