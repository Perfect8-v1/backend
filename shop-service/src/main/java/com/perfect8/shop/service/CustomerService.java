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
 * Core customer management functionality
 *
 * Magnum Opus Principles:
 * - Readable variable names (customerId not customerEmailDTOId, not customerIdLong)
 * - NO backward compatibility - built right from start
 * - Use createdDate/updatedDate (consistent with Customer entity)
 * - Frontend handles passwordHash - NO passwordEncoder in this service
 *
 * SECURITY MODEL:
 * - Frontend creates passwordHash (NEVER sends plain password)
 * - Backend stores and compares passwordHash only
 * - NO passwordEncoder - Frontend handles hashing
 * 
 * FIXED: isDefaultAddress() instead of getDefaultAddress() (Lombok boolean naming)
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
     * Get customer entity by email (for internal use)
     * Returns the entity, not DTO
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
     * Get customer DTO by email (for external use)
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerDTOByEmail(String email) {
        return convertToDTO(getCustomerByEmail(email));
    }

    /**
     * Register new customer
     * FIXED: Frontend sends passwordHash - store directly (NO encoding)
     */
    @Transactional
    public CustomerDTO registerCustomer(CustomerRegistrationDTO registrationDTO) {
        log.debug("Creating new customer with email: {}", registrationDTO.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + registrationDTO.getEmail());
        }

        // FIXED: Store passwordHash directly from frontend (NO encoding)
        Customer customer = Customer.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .passwordHash(registrationDTO.getPasswordHash())
                .phone(registrationDTO.getPhone())
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
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", savedCustomer.getEmail(), e);
        }

        return convertToDTO(savedCustomer);
    }

    /**
     * Update customer details
     * FIXED: Frontend sends passwordHash for password changes (NO encoding)
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

        // Handle password change if requested
        // FIXED: Compare and store passwordHash directly (NO passwordEncoder)
        if (updateDTO.isPasswordChangeRequested()) {
            // Verify current passwordHash
            if (!customer.getPasswordHash().equals(updateDTO.getCurrentPasswordHash())) {
                throw new UnauthorizedAccessException("Current passwordHash is incorrect");
            }
            // Store new passwordHash directly
            customer.setPasswordHash(updateDTO.getNewPasswordHash());
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
     * Get all customers (admin only)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all customers");
        return customerRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Search customers by email, name, or phone (admin only)
     * FIXED: Accepts multiple optional search criteria
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> searchCustomers(String email, String name, String phone, Pageable pageable) {
        log.debug("Searching customers - email: {}, name: {}, phone: {}", email, name, phone);
        
        // Build search term from provided parameters
        StringBuilder searchTermBuilder = new StringBuilder();
        if (email != null && !email.isEmpty()) {
            searchTermBuilder.append(email);
        }
        if (name != null && !name.isEmpty()) {
            if (searchTermBuilder.length() > 0) searchTermBuilder.append(" ");
            searchTermBuilder.append(name);
        }
        if (phone != null && !phone.isEmpty()) {
            if (searchTermBuilder.length() > 0) searchTermBuilder.append(" ");
            searchTermBuilder.append(phone);
        }
        
        String searchTerm = searchTermBuilder.toString().trim();
        if (searchTerm.isEmpty()) {
            // If no search criteria provided, return all customers
            return getAllCustomers(pageable);
        }
        
        return customerRepository.searchCustomers(searchTerm, pageable).map(this::convertToDTO);
    }
    
    /**
     * Delete customer permanently (admin only)
     * WARNING: This permanently deletes the customer and all associated data
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.debug("Permanently deleting customer with ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        
        customerRepository.delete(customer);
        log.warn("Customer permanently deleted with ID: {}", customerId);
    }
    
    /**
     * Toggle customer active status (admin only)
     */
    @Transactional
    public CustomerDTO toggleCustomerStatus(Long customerId) {
        log.debug("Toggling status for customer ID: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        
        customer.setActive(!customer.isActive());
        Customer updatedCustomer = customerRepository.save(customer);
        
        log.info("Customer status toggled for ID: {} - New status: {}", customerId, updatedCustomer.isActive());
        return convertToDTO(updatedCustomer);
    }
    
    /**
     * Get recent customers (admin only)
     * Returns a simple list of recent customers (not paginated)
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getRecentCustomers(int limit) {
        log.debug("Fetching {} recent customers", limit);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit, 
            org.springframework.data.domain.Sort.by("createdDate").descending());
        return customerRepository.findAllByOrderByCreatedDateDesc(pageable)
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Get customer statistics (admin only)
     */
    @Transactional(readOnly = true)
    public CustomerStatsDTO getCustomerStatistics() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByActiveTrue();
        long verifiedCustomers = customerRepository.countByEmailVerifiedTrue();

        return CustomerStatsDTO.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .verifiedCustomers(verifiedCustomers)
                .inactiveCustomers(totalCustomers - activeCustomers)
                .build();
    }

    /**
     * Add address to customer
     * FIXED: state field now included
     */
    @Transactional
    public AddressDTO addCustomerAddress(Long customerId, AddressDTO addressDTO) {
        log.debug("Adding address for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Cannot add address to inactive customer");
        }

        // If first address, make it default for BOTH billing and shipping
        boolean isFirstAddress = customer.getAddresses().isEmpty();
        String addressType = isFirstAddress ? "BOTH" :
                (addressDTO.getAddressType() != null ? addressDTO.getAddressType() : "BOTH");

        Address address = Address.builder()
                .customer(customer)
                .street(addressDTO.getStreetAddress())
                .apartment(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())  // FIXED: state field included
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .addressType(addressType)
                .defaultAddress(isFirstAddress)
                .build();

        customer.addAddress(address);
        Address savedAddress = addressRepository.save(address);

        log.info("Address added successfully for customer ID: {}", customerId);
        return convertToAddressDTO(savedAddress);
    }

    /**
     * Update customer address
     * FIXED: state field now included
     */
    @Transactional
    public AddressDTO updateCustomerAddress(Long customerId, Long addressId, AddressDTO addressDTO) {
        log.debug("Updating address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        address.setStreet(addressDTO.getStreetAddress());
        address.setApartment(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());  // FIXED: state field included
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully for customer ID: {}", customerId);

        return convertToAddressDTO(updatedAddress);
    }

    /**
     * Delete customer address
     */
    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        log.debug("Deleting address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        customer.removeAddress(address);
        addressRepository.delete(address);

        log.info("Address deleted successfully for customer ID: {}", customerId);
    }

    /**
     * Set default address for customer
     * FIXED: setDefaultAddress() now uses boolean primitive
     */
    @Transactional
    public AddressDTO setDefaultAddress(Long customerId, Long addressId) {
        log.debug("Setting default address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address targetAddress = null;
        for (Address address : customer.getAddresses()) {
            if (address.getAddressId().equals(addressId)) {
                address.setDefaultAddress(true);
                targetAddress = address;
            } else {
                address.setDefaultAddress(false);
            }
        }

        if (targetAddress == null) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressId);
        }

        customerRepository.save(customer);
        log.info("Default address set for customer ID: {}", customerId);

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

    /**
     * Verify customer email
     */
    @Transactional
    public void verifyCustomerEmail(String email, String verificationToken) {
        log.debug("Verifying email for: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        // Check if token matches
        if (customer.getEmailVerificationToken() == null ||
                !customer.getEmailVerificationToken().equals(verificationToken)) {
            throw new UnauthorizedAccessException("Invalid verification token");
        }

        // Check if token is expired (24 hours)
        if (customer.getEmailVerificationSentDate() != null &&
                customer.getEmailVerificationSentDate().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new UnauthorizedAccessException("Verification token has expired");
        }

        customer.setEmailVerified(true);
        customer.setEmailVerificationToken(null);
        customer.setEmailVerificationSentDate(null);
        customerRepository.save(customer);

        log.info("Email verified successfully for customer: {}", email);
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.debug("Resending verification email to: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

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
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", customer.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Convert Customer entity to DTO with null safety
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
                .createdDate(customer.getCreatedDate())
                .updatedDate(customer.getUpdatedDate())
                .lastLoginDate(customer.getLastLoginDate())
                .build();
    }

    /**
     * Convert Address entity to DTO with null safety
     * FIXED: isDefaultAddress() instead of getDefaultAddress() (Lombok boolean naming)
     */
    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Cannot convert null Address to DTO");
        }

        String addressType = address.getAddressType() != null ? address.getAddressType() : "BOTH";
        boolean isDefault = address.isDefaultAddress();  // FIXED: isDefaultAddress() (Lombok boolean getter)

        return AddressDTO.builder()
                .addressId(address.getAddressId())
                .streetAddress(address.getStreet())
                .addressLine2(address.getApartment())
                .city(address.getCity())
                .state(address.getState())  // FIXED: state field included
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
