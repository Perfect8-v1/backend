package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Address;
import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.entity.Order;
import com.perfect8.common.enums.OrderStatus;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Service for Shop Service
 * Version 1.0 - Core customer management functionality
 * NO BACKWARD COMPATIBILITY - Built right from the start!
 *
 * FIXED: Uses correct field names:
 * - Customer: phone (not phoneNumber), passwordHash
 * - Address: street, apartment, isDefault (Boolean)
 * - CustomerRepository: findByActiveTrue(pageable)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerIdLong) {
        log.debug("Fetching customer with ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return convertToDTO(customer);
    }

    /**
     * Get customer entity by email (for internal use)
     * Returns the entity, not DTO
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String customerEmail) {
        log.debug("Fetching customer with email: {}", customerEmail);

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + customerEmail));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return customer;
    }

    /**
     * Get customer DTO by email (for external use)
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerDTOByEmail(String customerEmail) {
        return convertToDTO(getCustomerByEmail(customerEmail));
    }

    /**
     * Register new customer - Used by CustomerController
     * FIXED: Uses correct field names (phone, passwordHash)
     */
    @Transactional
    public CustomerDTO registerCustomer(CustomerRegistrationDTO registrationDTO) {
        log.debug("Creating new customer with email: {}", registrationDTO.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + registrationDTO.getEmail());
        }

        // FIXED: Use correct field names
        Customer customer = Customer.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .passwordHash(passwordEncoder.encode(registrationDTO.getPassword()))
                .phone(registrationDTO.getPhoneNumber())
                .role("ROLE_USER")
                .active(true)
                .emailVerified(false)
                .newsletterSubscribed(registrationDTO.getNewsletterSubscribed())
                .marketingConsent(registrationDTO.getMarketingConsent())
                .preferredLanguage(registrationDTO.getPreferredLanguage())
                .preferredCurrency(registrationDTO.getPreferredCurrency())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getCustomerId());

        // Send welcome email
        try {
            String fullName = savedCustomer.getFirstName() + " " + savedCustomer.getLastName();
            emailService.sendWelcomeEmail(savedCustomer.getEmail(), fullName);
        } catch (Exception emailException) {
            log.error("Failed to send welcome email to: {}", savedCustomer.getEmail(), emailException);
        }

        return convertToDTO(savedCustomer);
    }

    /**
     * Update customer details
     * FIXED: Uses setters that work with actual fields
     */
    @Transactional
    public CustomerDTO updateCustomer(Long customerIdLong, CustomerUpdateDTO updateDTO) {
        log.debug("Updating customer with ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

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
        if (updateDTO.getPhoneNumber() != null) {
            customer.setPhone(updateDTO.getPhoneNumber());
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

        // Handle password change if requested
        if (updateDTO.isPasswordChangeRequested()) {
            // Verify current password
            if (!passwordEncoder.matches(updateDTO.getCurrentPassword(), customer.getPasswordHash())) {
                throw new UnauthorizedAccessException("Current password is incorrect");
            }
            customer.setPasswordHash(passwordEncoder.encode(updateDTO.getNewPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", customerIdLong);

        return convertToDTO(updatedCustomer);
    }

    /**
     * Deactivate customer account
     */
    @Transactional
    public void deactivateCustomer(Long customerIdLong) {
        log.debug("Deactivating customer with ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        customer.setActive(false);
        customerRepository.save(customer);

        log.info("Customer deactivated successfully with ID: {}", customerIdLong);
    }

    /**
     * Delete customer (soft delete in v1.0)
     */
    @Transactional
    public void deleteCustomer(Long customerIdLong) {
        log.debug("Soft deleting customer with ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        customer.setActive(false);
        customerRepository.save(customer);

        log.info("Customer deactivated (soft delete) with ID: {}", customerIdLong);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String emailAddress) {
        return customerRepository.existsByEmail(emailAddress);
    }

    /**
     * Get all customers (paginated) - Admin only
     * FIXED: Uses correct repository method name
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all customers, page: {}", pageable.getPageNumber());

        Page<Customer> customers = customerRepository.findByActiveTrue(pageable);
        return customers.map(this::convertToDTO);
    }

    /**
     * Search customers - Admin only
     * Simplified for v1.0 - advanced search in v2.0
     * FIXED: Uses correct repository method name
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> searchCustomers(String searchEmail, String searchName,
                                             String searchPhone, Pageable pageable) {
        log.debug("Searching customers with criteria - email: {}, name: {}, phone: {}",
                searchEmail, searchName, searchPhone);

        // Simple implementation for v1.0 - just return all active customers
        // TODO v2.0: Add proper search with Specifications or QueryDSL
        Page<Customer> customers = customerRepository.findByActiveTrue(pageable);

        return customers.map(this::convertToDTO);
    }

    /**
     * Toggle customer status - Admin only
     */
    @Transactional
    public CustomerDTO toggleCustomerStatus(Long customerIdLong) {
        log.debug("Toggling status for customer: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        customer.setActive(!customer.isActive()); // Toggle status
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer status toggled for ID: {}", customerIdLong);
        return convertToDTO(updatedCustomer);
    }

    /**
     * Get recent customers - Admin only
     * Simplified for v1.0
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getRecentCustomers(int limit) {
        log.debug("Fetching {} recent customers", limit);

        // Simple implementation for v1.0 - get all active customers and limit
        List<Customer> allCustomers = customerRepository.findAll();

        return allCustomers.stream()
                .filter(Customer::isActive)
                .sorted((c1, c2) -> {
                    if (c2.getCreatedAt() == null || c1.getCreatedAt() == null) {
                        return 0;
                    }
                    return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                })
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Add address to customer
     * FIXED: Uses correct Address field names (street, apartment, isDefault)
     */
    @Transactional
    public AddressDTO addCustomerAddress(Long customerIdLong, AddressDTO addressDTO) {
        log.debug("Adding address for customer ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Cannot add address to inactive customer");
        }

        // If first address, make it default for BOTH billing and shipping
        boolean isFirstAddress = customer.getAddresses().isEmpty();
        String addressType = isFirstAddress ? "BOTH" :
                (addressDTO.getAddressType() != null ? addressDTO.getAddressType() : "BOTH");

        // FIXED: Use correct field names from Address entity
        Address address = Address.builder()
                .customer(customer)
                .street(addressDTO.getStreetAddress())
                .apartment(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .addressType(addressType)
                .isDefault(isFirstAddress)
                .build();

        customer.addAddress(address);
        Address savedAddress = addressRepository.save(address);

        log.info("Address added successfully for customer ID: {}", customerIdLong);
        return convertToAddressDTO(savedAddress);
    }

    /**
     * Update customer address
     * FIXED: Uses correct Address field names and setters
     */
    @Transactional
    public AddressDTO updateCustomerAddress(Long customerIdLong, Long addressIdLong, AddressDTO addressDTO) {
        log.debug("Updating address {} for customer {}", addressIdLong, customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        Address address = addressRepository.findById(addressIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressIdLong));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerIdLong)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        // FIXED: Use correct setter names
        address.setStreet(addressDTO.getStreetAddress());
        address.setApartment(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully for customer ID: {}", customerIdLong);

        return convertToAddressDTO(updatedAddress);
    }

    /**
     * Delete customer address
     */
    @Transactional
    public void deleteCustomerAddress(Long customerIdLong, Long addressIdLong) {
        log.debug("Deleting address {} for customer {}", addressIdLong, customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        Address address = addressRepository.findById(addressIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressIdLong));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerIdLong)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        customer.removeAddress(address);
        customerRepository.save(customer);
        addressRepository.delete(address);

        log.info("Address deleted successfully for customer ID: {}", customerIdLong);
    }

    /**
     * Set default address for customer
     * FIXED: Uses correct method name setIsDefault()
     */
    @Transactional
    public AddressDTO setDefaultAddress(Long customerIdLong, Long addressIdLong) {
        log.debug("Setting default address {} for customer {}", addressIdLong, customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        Address targetAddress = null;

        // Clear current defaults and set new one
        for (Address addr : customer.getAddresses()) {
            if (addr.getAddressId().equals(addressIdLong)) {
                // FIXED: Use correct method name
                addr.setIsDefault(true);
                targetAddress = addr;
            } else {
                addr.setIsDefault(false);
            }
        }

        if (targetAddress == null) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressIdLong);
        }

        customerRepository.save(customer);
        log.info("Default address set for customer ID: {}", customerIdLong);

        return convertToAddressDTO(targetAddress);
    }

    /**
     * Get customer addresses
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getCustomerAddresses(Long customerIdLong) {
        log.debug("Fetching addresses for customer ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        return customer.getAddresses().stream()
                .map(this::convertToAddressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get customer order history
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(Long customerIdLong, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}", customerIdLong);

        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerIdLong));

        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
        return orders.map(this::convertToOrderDTO);
    }

    /**
     * Verify customer email
     */
    @Transactional
    public void verifyCustomerEmail(String customerEmail, String verificationToken) {
        log.debug("Verifying email for: {}", customerEmail);

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + customerEmail));

        // Check if token matches
        if (customer.getEmailVerificationToken() == null ||
                !customer.getEmailVerificationToken().equals(verificationToken)) {
            throw new UnauthorizedAccessException("Invalid verification token");
        }

        // Check if token is expired (24 hours)
        if (customer.getEmailVerificationSentAt() != null &&
                customer.getEmailVerificationSentAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new UnauthorizedAccessException("Verification token has expired");
        }

        customer.setEmailVerified(true);
        customer.setEmailVerificationToken(null);
        customer.setEmailVerificationSentAt(null);
        customerRepository.save(customer);

        log.info("Email verified successfully for customer: {}", customerEmail);
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String customerEmail) {
        log.debug("Resending verification email to: {}", customerEmail);

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + customerEmail));

        if (customer.isEmailVerified()) {
            throw new UnauthorizedAccessException("Email already verified");
        }

        // Generate new token
        customer.generateEmailVerificationToken();
        customerRepository.save(customer);

        // Send verification email
        try {
            String verificationToken = customer.getEmailVerificationToken();
            emailService.sendEmailVerification(customer.getEmail(), verificationToken);
        } catch (Exception emailException) {
            log.error("Failed to send verification email to: {}", customer.getEmail(), emailException);
            throw new RuntimeException("Failed to send verification email", emailException);
        }
    }

    /**
     * Convert Customer entity to DTO with null safety
     * Uses alias getters that work correctly
     */
    private CustomerDTO convertToDTO(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Cannot convert null Customer to DTO");
        }

        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhone())
                .role(customer.getRole())
                .isActive(customer.isActive())
                .isEmailVerified(customer.isEmailVerified())
                .newsletterSubscribed(customer.getNewsletterSubscribed())
                .marketingConsent(customer.getMarketingConsent())
                .preferredLanguage(customer.getPreferredLanguage())
                .preferredCurrency(customer.getPreferredCurrency())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .lastLoginDate(customer.getLastLoginAt())
                .build();
    }

    /**
     * Convert Address entity to DTO with null safety
     * FIXED: Uses correct getter names (getStreet, getApartment, getIsDefault)
     */
    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Cannot convert null Address to DTO");
        }

        // FIXED: Use correct getter names
        String addressType = address.getAddressType() != null ? address.getAddressType() : "BOTH";
        boolean isDefault = address.getIsDefault() != null && address.getIsDefault();

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
     * Convert Order to OrderDTO with null safety
     * NOTE: Simplified for v1.0 - only core fields
     * Will be expanded when Order.java is fixed
     */
    private OrderDTO convertToOrderDTO(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot convert null Order to DTO");
        }

        // Validate required fields
        if (order.getOrderId() == null) {
            throw new IllegalStateException("Order missing orderId");
        }

        if (order.getCustomer() == null) {
            throw new IllegalStateException("Order " + order.getOrderId() + " missing customer");
        }

        if (order.getOrderStatus() == null) {
            throw new IllegalStateException("Order " + order.getOrderId() + " missing status");
        }

        // Set safe defaults for nullable fields
        String orderNumber = order.getOrderNumber() != null ? order.getOrderNumber() : "ORD-" + order.getOrderId();
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        LocalDateTime createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        LocalDateTime updatedAt = order.getUpdatedAt() != null ? order.getUpdatedAt() : createdAt;

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .orderNumber(orderNumber)
                .customerId(order.getCustomer().getCustomerId())
                .status(order.getOrderStatus().toString())
                .totalAmount(totalAmount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}