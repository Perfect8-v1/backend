package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Address;
import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.exception.CustomerNotFoundException;
import com.perfect8.shop.exception.EmailAlreadyExistsException;
import com.perfect8.shop.exception.ResourceNotFoundException;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import com.perfect8.shop.repository.AddressRepository;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Service - Version 1.0
 * Customer profile management (NOT authentication)
 *
 * UPDATED (2025-11-20):
 * - Removed all auth logic (handled by admin-service)
 * - Added userId linking for registered users
 * - Supports guest checkout (userId = null)
 * - Fixed boolean naming (isNewsletterSubscribed, isMarketingConsent)
 *
 * Magnum Opus Principles:
 * - Readable variable names
 * - NO backward compatibility
 * - Use createdDate/updatedDate
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    // ==================== Customer Retrieval ====================

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        log.debug("Fetching customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return convertToDTO(customer);
    }

    /**
     * Get customer by userId (from admin-service)
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByUserId(Long userId) {
        log.debug("Fetching customer with userId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with userId: " + userId));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return convertToDTO(customer);
    }

    /**
     * Get customer entity by email (for internal use)
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return customer;
    }

    /**
     * Get customer DTO by email
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerDTOByEmail(String email) {
        return convertToDTO(getCustomerByEmail(email));
    }

    /**
     * Check if customer exists by email
     */
    @Transactional(readOnly = true)
    public boolean customerExistsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Check if customer exists by userId
     */
    @Transactional(readOnly = true)
    public boolean customerExistsByUserId(Long userId) {
        return customerRepository.findByUserId(userId).isPresent();
    }

    // ==================== Customer Creation ====================

    /**
     * Create customer profile for registered user
     * Called after user registers in admin-service
     */
    @Transactional
    public CustomerDTO createCustomerProfile(Long userId, String email, String firstName, String lastName) {
        log.debug("Creating customer profile for userId: {}", userId);

        // Check if email already exists
        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }

        Customer customer = Customer.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .isActive(true)
                .isEmailVerified(false)
                .newsletterSubscribed(false)
                .marketingConsent(false)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer profile created with ID: {} for userId: {}", savedCustomer.getCustomerId(), userId);

        // Send welcome email
        try {
            String fullName = savedCustomer.getFirstName() + " " + savedCustomer.getLastName();
            emailService.sendWelcomeEmail(savedCustomer.getEmail(), fullName);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", savedCustomer.getEmail(), e);
        }

        return convertToDTO(savedCustomer);
    }

    /**
     * Create guest customer (for guest checkout)
     */
    @Transactional
    public CustomerDTO createGuestCustomer(String email, String firstName, String lastName, String phone) {
        log.debug("Creating guest customer with email: {}", email);

        // Check if email already exists
        if (customerRepository.existsByEmail(email)) {
            // Return existing customer for guest checkout
            Customer existing = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
            return convertToDTO(existing);
        }

        Customer customer = Customer.builder()
                .userId(null) // Guest - no user account
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .isActive(true)
                .isEmailVerified(false)
                .newsletterSubscribed(false)
                .marketingConsent(false)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Guest customer created with ID: {}", savedCustomer.getCustomerId());

        return convertToDTO(savedCustomer);
    }

    /**
     * Link existing customer to user account
     * Used when guest creates account after checkout
     */
    @Transactional
    public CustomerDTO linkCustomerToUser(Long customerId, Long userId) {
        log.debug("Linking customer {} to userId {}", customerId, userId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (customer.getUserId() != null) {
            throw new UnauthorizedAccessException("Customer already linked to a user account");
        }

        customer.setUserId(userId);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer {} linked to userId {}", customerId, userId);
        return convertToDTO(savedCustomer);
    }

    // ==================== Customer Update ====================

    /**
     * Update customer details
     */
    @Transactional
    public CustomerDTO updateCustomer(Long customerId, CustomerUpdateDTO updateDTO) {
        log.debug("Updating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Cannot update inactive customer");
        }

        // Update fields if provided
        if (updateDTO.getFirstName() != null) {
            customer.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            customer.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhone() != null) {
            customer.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getNewsletterSubscribed() != null) {
            customer.setNewsletterSubscribed(updateDTO.getNewsletterSubscribed());
        }
        if (updateDTO.getMarketingConsent() != null) {
            customer.setMarketingConsent(updateDTO.getMarketingConsent());
        }
        if (updateDTO.getPreferredLanguage() != null) {
            customer.setPreferredLanguage(updateDTO.getPreferredLanguage());
        }
        if (updateDTO.getPreferredCurrency() != null) {
            customer.setPreferredCurrency(updateDTO.getPreferredCurrency());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", customerId);

        return convertToDTO(updatedCustomer);
    }

    /**
     * Deactivate customer account
     */
    @Transactional
    public void deactivateCustomer(Long customerId) {
        log.debug("Deactivating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setActive(false);
        customerRepository.save(customer);

        log.info("Customer deactivated successfully with ID: {}", customerId);
    }

    /**
     * Reactivate customer account
     */
    @Transactional
    public void reactivateCustomer(Long customerId) {
        log.debug("Reactivating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setActive(true);
        customerRepository.save(customer);

        log.info("Customer reactivated successfully with ID: {}", customerId);
    }

    /**
     * Mark customer email as verified
     * Called by admin-service after email verification
     */
    @Transactional
    public void markEmailVerified(Long customerId) {
        log.debug("Marking email verified for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.verifyEmail();
        customerRepository.save(customer);

        log.info("Email marked as verified for customer ID: {}", customerId);
    }

    /**
     * Update last login time
     */
    @Transactional
    public void updateLastLogin(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.updateLastLoginTime();
        customerRepository.save(customer);
    }

    // ==================== Address Management ====================

    /**
     * Add address to customer
     */
    @Transactional
    public AddressDTO addCustomerAddress(Long customerId, AddressDTO addressDTO) {
        log.debug("Adding address for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = Address.builder()
                .customer(customer)
                .street(addressDTO.getStreetAddress())
                .apartment(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .addressType(addressDTO.getAddressType())
                .defaultAddress(addressDTO.isDefaultShipping() || addressDTO.isDefaultBilling())
                .build();

        // If this is set as default, unset other defaults
        if (address.isDefaultAddress()) {
            customer.getAddresses().forEach(a -> {
                if (a.getAddressType().equals(address.getAddressType())) {
                    a.setDefaultAddress(false);
                }
            });
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address added for customer ID: {}", customerId);

        return convertToAddressDTO(savedAddress);
    }

    /**
     * Update customer address
     */
    @Transactional
    public AddressDTO updateCustomerAddress(Long customerId, Long addressId, AddressDTO addressDTO) {
        log.debug("Updating address {} for customer ID: {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        // Update fields
        if (addressDTO.getStreetAddress() != null) {
            address.setStreet(addressDTO.getStreetAddress());
        }
        if (addressDTO.getAddressLine2() != null) {
            address.setApartment(addressDTO.getAddressLine2());
        }
        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }
        if (addressDTO.getState() != null) {
            address.setState(addressDTO.getState());
        }
        if (addressDTO.getPostalCode() != null) {
            address.setPostalCode(addressDTO.getPostalCode());
        }
        if (addressDTO.getCountry() != null) {
            address.setCountry(addressDTO.getCountry());
        }
        if (addressDTO.getAddressType() != null) {
            address.setAddressType(addressDTO.getAddressType());
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address {} updated for customer ID: {}", addressId, customerId);

        return convertToAddressDTO(savedAddress);
    }

    /**
     * Delete customer address
     */
    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        log.debug("Deleting address {} for customer ID: {}", addressId, customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        addressRepository.delete(address);
        log.info("Address {} deleted for customer ID: {}", addressId, customerId);
    }

    /**
     * Set default address
     */
    @Transactional
    public AddressDTO setDefaultAddress(Long customerId, Long addressId, String addressType) {
        log.debug("Setting default {} address {} for customer ID: {}", addressType, addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address targetAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!targetAddress.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        // Unset other defaults of same type
        customer.getAddresses().forEach(a -> {
            if (a.getAddressType().equals(addressType)) {
                a.setDefaultAddress(false);
                addressRepository.save(a);
            }
        });

        // Set this address as default
        targetAddress.setDefaultAddress(true);
        targetAddress.setAddressType(addressType);
        addressRepository.save(targetAddress);

        log.info("Address {} set as default {} for customer ID: {}", addressId, addressType, customerId);

        return convertToAddressDTO(targetAddress);
    }

    /**
     * Get customer addresses
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getCustomerAddresses(Long customerId) {
        log.debug("Fetching addresses for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        return customer.getAddresses().stream()
                .map(this::convertToAddressDTO)
                .collect(Collectors.toList());
    }

    // ==================== Order History ====================

    /**
     * Get customer order history
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedDateDesc(customer, pageable);
        return orders.map(this::convertToOrderDTO);
    }

    // ==================== Admin Methods ====================

    /**
     * Get all customers (admin only)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Get customer statistics
     */
    @Transactional(readOnly = true)
    public CustomerStatsDTO getCustomerStats() {
        long total = customerRepository.count();
        long active = customerRepository.countByActiveTrue();
        long verified = customerRepository.countByEmailVerifiedTrue();

        return CustomerStatsDTO.builder()
                .totalCustomers(total)
                .activeCustomers(active)
                .inactiveCustomers(total - active)
                .verifiedCustomers(verified)
                .build();
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert Customer entity to DTO
     */
    private CustomerDTO convertToDTO(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Cannot convert null Customer to DTO");
        }

        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .userId(customer.getUserId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhone())
                .isActive(customer.isActive())
                .isEmailVerified(customer.isEmailVerified())
                .newsletterSubscribed(customer.isNewsletterSubscribed())
                .marketingConsent(customer.isMarketingConsent())
                .preferredLanguage(customer.getPreferredLanguage())
                .preferredCurrency(customer.getPreferredCurrency())
                .createdDate(customer.getCreatedDate())
                .updatedDate(customer.getUpdatedDate())
                .lastLoginDate(customer.getLastLoginDate())
                .build();
    }

    /**
     * Convert Address entity to DTO
     */
    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Cannot convert null Address to DTO");
        }

        String addressType = address.getAddressType() != null ? address.getAddressType() : "BOTH";
        boolean isDefault = address.isDefaultAddress();

        return AddressDTO.builder()
                .addressId(address.getAddressId())
                .streetAddress(address.getStreet())
                .addressLine2(address.getApartment())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .addressType(addressType)
                .isDefaultShipping(isDefault && ("SHIPPING".equals(addressType) || "BOTH".equals(addressType)))
                .isDefaultBilling(isDefault && ("BILLING".equals(addressType) || "BOTH".equals(addressType)))
                .build();
    }

    /**
     * Convert Order to OrderDTO
     */
    private OrderDTO convertToOrderDTO(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot convert null Order to DTO");
        }

        String orderNumber = order.getOrderNumber() != null ? order.getOrderNumber() : "ORD-" + order.getOrderId();
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        LocalDateTime createdDate = order.getCreatedDate() != null ? order.getCreatedDate() : LocalDateTime.now();
        LocalDateTime updatedDate = order.getUpdatedDate() != null ? order.getUpdatedDate() : createdDate;

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .orderNumber(orderNumber)
                .customerId(order.getCustomer().getCustomerId())
                .status(order.getOrderStatus().toString())
                .totalAmount(totalAmount)
                .createdDate(createdDate)
                .updatedDate(updatedDate)
                .build();
    }

    /**
     * CustomerStatsDTO - Inner class for statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class CustomerStatsDTO {
        private long totalCustomers;
        private long activeCustomers;
        private long inactiveCustomers;
        private long verifiedCustomers;
    }
}
