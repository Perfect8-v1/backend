package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

    // Basic customer address queries
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId ORDER BY a.isDefault DESC, a.addressId DESC")
    List<Address> findByCustomerIdOrderByDefaultAddressDescCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId")
    List<Address> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT a FROM Address a WHERE a.addressId = :addressId AND a.customer.customerId = :customerId")
    Optional<Address> findByAddressIdAndCustomerId(@Param("addressId") Long addressId, @Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) FROM Address a WHERE a.customer.customerId = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    // Default address management
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.isDefault = true")
    Optional<Address> findByCustomerIdAndDefaultAddressTrue(@Param("customerId") Long customerId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.customer.customerId = :customerId AND a.isDefault = true")
    boolean existsByCustomerIdAndDefaultAddressTrue(@Param("customerId") Long customerId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = :isDefault WHERE a.customer.customerId = :customerId")
    void updateDefaultAddressForCustomer(@Param("customerId") Long customerId, @Param("isDefault") boolean isDefault);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.customerId = :customerId AND a.addressId != :excludeAddressId")
    void clearOtherDefaultAddresses(@Param("customerId") Long customerId, @Param("excludeAddressId") Long excludeAddressId);

    // Address type queries
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.addressType = :addressType")
    List<Address> findByCustomerIdAndAddressType(@Param("customerId") Long customerId, @Param("addressType") String addressType);

    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.addressType = :addressType AND a.isDefault = true")
    Optional<Address> findByCustomerIdAndAddressTypeAndDefaultAddressTrue(@Param("customerId") Long customerId, @Param("addressType") String addressType);

    List<Address> findByAddressType(String addressType);

    // Geographic queries
    List<Address> findByCountry(String country);

    List<Address> findByCountryAndState(String country, String state);

    List<Address> findByCountryAndStateAndCity(String country, String state, String city);

    Page<Address> findByCountry(String country, Pageable pageable);

    // Postal code queries
    List<Address> findByPostalCode(String postalCode);

    List<Address> findByPostalCodeStartingWith(String postalCodePrefix);

    // Search queries
    @Query("SELECT a FROM Address a WHERE " +
            "LOWER(a.street) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.state) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Address> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Statistics and analytics
    @Query("SELECT a.country, COUNT(a) FROM Address a GROUP BY a.country ORDER BY COUNT(a) DESC")
    List<Object[]> findAddressCountByCountry();

    @Query("SELECT a.state, COUNT(a) FROM Address a WHERE a.country = :country GROUP BY a.state ORDER BY COUNT(a) DESC")
    List<Object[]> findAddressCountByState(@Param("country") String country);

    @Query("SELECT a.city, COUNT(a) FROM Address a WHERE a.country = :country AND a.state = :state GROUP BY a.city ORDER BY COUNT(a) DESC")
    List<Object[]> findAddressCountByCity(@Param("country") String country, @Param("state") String state);

    @Query("SELECT a.addressType, COUNT(a) FROM Address a GROUP BY a.addressType")
    List<Object[]> findAddressCountByType();

    // Validation and data quality
    @Query("SELECT a FROM Address a WHERE a.postalCode IS NULL OR a.postalCode = ''")
    List<Address> findAddressesWithoutPostalCode();

    @Query("SELECT a FROM Address a WHERE a.state IS NULL OR a.state = ''")
    List<Address> findAddressesWithoutState();

    @Query("SELECT a FROM Address a WHERE " +
            "a.street IS NULL OR a.street = '' OR " +
            "a.city IS NULL OR a.city = '' OR " +
            "a.country IS NULL OR a.country = ''")
    List<Address> findIncompleteAddresses();

    // Customer address management
    @Query("SELECT COUNT(a) FROM Address a WHERE a.customer.customerId = :customerId AND a.addressType = :addressType")
    long countByCustomerIdAndAddressType(@Param("customerId") Long customerId, @Param("addressType") String addressType);

    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.addressType IN ('BILLING', 'BOTH')")
    List<Address> findBillingAddressesByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND a.addressType IN ('SHIPPING', 'BOTH')")
    List<Address> findShippingAddressesByCustomerId(@Param("customerId") Long customerId);

    // Address verification and validation
    @Query("SELECT DISTINCT a.postalCode FROM Address a WHERE a.country = :country AND a.state = :state ORDER BY a.postalCode")
    List<String> findPostalCodesByCountryAndState(@Param("country") String country, @Param("state") String state);

    @Query("SELECT DISTINCT a.city FROM Address a WHERE a.country = :country AND a.state = :state ORDER BY a.city")
    List<String> findCitiesByCountryAndState(@Param("country") String country, @Param("state") String state);

    @Query("SELECT DISTINCT a.state FROM Address a WHERE a.country = :country ORDER BY a.state")
    List<String> findStatesByCountry(@Param("country") String country);

    @Query("SELECT DISTINCT a.country FROM Address a ORDER BY a.country")
    List<String> findAllCountries();

    // Duplicate detection
    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId AND " +
            "LOWER(a.street) = LOWER(:street) AND " +
            "LOWER(a.city) = LOWER(:city) AND " +
            "LOWER(a.postalCode) = LOWER(:postalCode) AND " +
            "LOWER(a.country) = LOWER(:country)")
    List<Address> findPotentialDuplicates(@Param("customerId") Long customerId,
                                          @Param("street") String street,
                                          @Param("city") String city,
                                          @Param("postalCode") String postalCode,
                                          @Param("country") String country);

    // International vs domestic
    @Query("SELECT COUNT(a) FROM Address a WHERE a.country = :domesticCountry")
    long countDomesticAddresses(@Param("domesticCountry") String domesticCountry);

    @Query("SELECT COUNT(a) FROM Address a WHERE a.country != :domesticCountry")
    long countInternationalAddresses(@Param("domesticCountry") String domesticCountry);

    // Recent addresses - removed queries using createdDate/updatedDate as these fields don't exist in Address entity
    @Query("SELECT a FROM Address a ORDER BY a.addressId DESC")
    List<Address> findRecentAddresses(Pageable pageable);

    @Query("SELECT a FROM Address a WHERE a.customer.customerId = :customerId ORDER BY a.addressId DESC")
    List<Address> findRecentlyUpdatedByCustomer(@Param("customerId") Long customerId, Pageable pageable);

    // Note: Removed delivery instructions and phone queries as these fields don't exist in Address entity

    // Advanced filtering
    @Query("SELECT a FROM Address a WHERE " +
            "(:customerId IS NULL OR a.customer.customerId = :customerId) AND " +
            "(:addressType IS NULL OR a.addressType = :addressType) AND " +
            "(:country IS NULL OR LOWER(a.country) = LOWER(:country)) AND " +
            "(:state IS NULL OR LOWER(a.state) = LOWER(:state)) AND " +
            "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:postalCode IS NULL OR a.postalCode LIKE CONCAT(:postalCode, '%')) AND " +
            "(:isDefault IS NULL OR a.isDefault = :isDefault)")
    Page<Address> findAddressesWithFilters(
            @Param("customerId") Long customerId,
            @Param("addressType") String addressType,
            @Param("country") String country,
            @Param("state") String state,
            @Param("city") String city,
            @Param("postalCode") String postalCode,
            @Param("isDefault") Boolean isDefault,
            Pageable pageable
    );

    // Address usage tracking (for order history)
    @Query("SELECT a, COUNT(o) as orderCount FROM Address a LEFT JOIN Order o ON " +
            "(o.shippingAddressLine1 LIKE CONCAT('%', a.street, '%') OR " +
            "o.billingAddressLine1 LIKE CONCAT('%', a.street, '%')) " +
            "WHERE a.customer.customerId = :customerId " +
            "GROUP BY a ORDER BY orderCount DESC")
    List<Object[]> findAddressUsageByCustomer(@Param("customerId") Long customerId);

    // Shipping zones and logistics
    @Query("SELECT a.postalCode, COUNT(a) FROM Address a WHERE a.country = :country " +
            "GROUP BY a.postalCode HAVING COUNT(a) > :threshold ORDER BY COUNT(a) DESC")
    List<Object[]> findHighVolumePostalCodes(@Param("country") String country, @Param("threshold") int threshold);

    @Query("SELECT SUBSTRING(a.postalCode, 1, :prefixLength), COUNT(a) FROM Address a " +
            "WHERE a.country = :country GROUP BY SUBSTRING(a.postalCode, 1, :prefixLength) " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findPostalCodePrefixDistribution(@Param("country") String country, @Param("prefixLength") int prefixLength);
}