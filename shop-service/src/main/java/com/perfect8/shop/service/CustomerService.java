package com.perfect8.shop.service;

import com.perfect8.shop.dto.AddressDTO;
import com.perfect8.shop.dto.CustomerDTO;
import com.perfect8.shop.dto.CustomerRegistrationDTO;
import com.perfect8.shop.dto.CustomerUpdateDTO;
import com.perfect8.shop.dto.OrderDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Customer Service for Shop Service
 * Version 1.0 - Core customer management functionality
 * Handles customer CRUD operations, address management, and order history
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

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
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        if (!customer.isActive()) {
            throw new CustomerNotFoundException("Customer account is inactive");
        }

        return convertToDTO(customer);
    }

    /**
     * Get all active customers (Admin only)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllActiveCustomers(Pageable pageable) {
        log.debug("Fetching all active customers, page: {}", pageable.getPageNumber());

        Page<Customer> customers = customerRepository.findByIsActiveTrue(pageable);
        return customers.map(this::convertToDTO);
    }

    /**
     * Create new customer
     */
    @Transactional
    public CustomerDTO createCustomer(CustomerRegistrationDTO registrationDTO) {
        log.debug("Creating new customer with email: {}", registrationDTO.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + registrationDTO.getEmail());
        }

        // Generate unique customer ID
        String customerBusinessId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create new customer
        Customer customer = new Customer();
        customer.setCustomerId(customerBusinessId); // This is the String business ID
        customer.setFirstName(registrationDTO.getFirstName());
        customer.setLastName(registrationDTO.getLastName());
        customer.setEmail(registrationDTO.getEmail());
        customer.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        customer.setPhoneNumber(registrationDTO.getPhoneNumber());
        customer.setRole("ROLE_CUSTOMER");
        customer.setIsActive(true);
        customer.setIsEmailVerified(false);
        customer.setCreatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());

        return convertToDTO(savedCustomer);
    }

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
        if (updateDTO.getPhoneNumber() != null) {
            customer.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        customer.setUpdatedAt(LocalDateTime.now());
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

        customer.setIsActive(false);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Customer deactivated successfully with ID: {}", customerId);
    }

    /**
     * Delete customer (permanent delete - use with caution)
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.debug("Deleting customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        // In version 1.0, we'll just deactivate instead of soft delete
        customer.setIsActive(false);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Customer deactivated (soft delete) with ID: {}", customerId);
    }

    /**
     * Add address to customer
     */
    @Transactional
    public AddressDTO addCustomerAddress(Long customerId, AddressDTO addressDTO) {
        log.debug("Adding address for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Cannot add address to inactive customer");
        }

        Address address = convertToAddressEntity(addressDTO);
        address.setCustomer(customer);

        // Set as default if it's the first address
        boolean isFirstAddress = customer.getAddresses() == null || customer.getAddresses().isEmpty();
        if (isFirstAddress) {
            address.setDefaultShipping(true);
            address.setDefaultBilling(true);
        }

        Address savedAddress = addressRepository.save(address);

        // Add address to customer's collection
        customer.getAddresses().add(savedAddress);
        customerRepository.save(customer);

        log.info("Address added successfully for customer ID: {}", customerId);
        return convertToAddressDTO(savedAddress);
    }

    /**
     * Update customer address
     */
    @Transactional
    public AddressDTO updateCustomerAddress(Long customerId, Long addressId, AddressDTO addressDTO) {
        log.debug("Updating address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        // Update address fields - check both addressLine1 and street for compatibility
        if (addressDTO.getStreetAddress() != null) {
            address.setAddressLine1(addressDTO.getStreetAddress());
            address.setStreet(addressDTO.getStreetAddress());
        }
        address.setAddressLine2(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setAddressType(addressDTO.getAddressType());

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
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        // Remove from customer's collection
        customer.getAddresses().remove(address);
        customerRepository.save(customer);

        // Delete the address
        addressRepository.delete(address);

        log.info("Address deleted successfully for customer ID: {}", customerId);
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
     * Set default shipping address
     */
    @Transactional
    public void setDefaultShippingAddress(Long customerId, Long addressId) {
        log.debug("Setting default shipping address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address newDefaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!newDefaultAddress.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        // Clear current default shipping address
        for (Address addr : customer.getAddresses()) {
            if (addr.isDefaultShipping()) {
                addr.setDefaultShipping(false);
            }
        }

        // Set new default
        newDefaultAddress.setDefaultShipping(true);
        addressRepository.save(newDefaultAddress);

        log.info("Default shipping address set for customer ID: {}", customerId);
    }

    /**
     * Set default billing address
     */
    @Transactional
    public void setDefaultBillingAddress(Long customerId, Long addressId) {
        log.debug("Setting default billing address {} for customer {}", addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address newDefaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!newDefaultAddress.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to this customer");
        }

        // Clear current default billing address
        for (Address addr : customer.getAddresses()) {
            if (addr.isDefaultBilling()) {
                addr.setDefaultBilling(false);
            }
        }

        // Set new default
        newDefaultAddress.setDefaultBilling(true);
        addressRepository.save(newDefaultAddress);

        log.info("Default billing address set for customer ID: {}", customerId);
    }

    /**
     * Get customer order history
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
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

        // Check if token matches and is not expired
        if (customer.getEmailVerificationToken() == null ||
                !customer.getEmailVerificationToken().equals(verificationToken)) {
            throw new UnauthorizedAccessException("Invalid verification token");
        }

        if (customer.getEmailVerificationTokenExpiry() != null &&
                customer.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedAccessException("Verification token has expired");
        }

        customer.setIsEmailVerified(true);
        customer.setEmailVerificationToken(null);
        customer.setEmailVerificationTokenExpiry(null);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Email verified successfully for customer: {}", email);
    }

    /**
     * Convert Customer entity to DTO
     */
    public CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerBusinessId()); // Get the String business ID
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setIsEmailVerified(customer.isEmailVerified());
        dto.setIsActive(customer.isActive());
        dto.setRegistrationDate(customer.getCreatedAt());
        dto.setLastLoginDate(customer.getLastLoginAt());
        dto.setCustomerSince(customer.getCreatedAt());

        // Calculate order count if orders are loaded
        if (customer.getOrders() != null) {
            dto.setHasOrders(!customer.getOrders().isEmpty());
            dto.setOrderCount(customer.getOrders().size());
        } else {
            dto.setHasOrders(false);
            dto.setOrderCount(0);
        }

        // Add addresses if loaded
        if (customer.getAddresses() != null) {
            dto.setAddresses(customer.getAddresses().stream()
                    .map(this::convertToAddressDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Convert Address entity to DTO
     */
    private AddressDTO convertToAddressDTO(Address address) {
        return AddressDTO.builder()
                .addressId(address.getAddressId())
                .streetAddress(address.getStreet() != null ? address.getStreet() : address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .isDefaultShipping(address.isDefaultShipping())
                .isDefaultBilling(address.isDefaultBilling())
                .build();
    }

    /**
     * Convert AddressDTO to entity
     */
    private Address convertToAddressEntity(AddressDTO dto) {
        return Address.builder()
                .addressLine1(dto.getStreetAddress())
                .street(dto.getStreetAddress()) // Set both for compatibility
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .addressType(dto.getAddressType())
                .build();
    }

    /**
     * Convert Order to OrderDTO (basic info only)
     */
    private OrderDTO convertToOrderDTO(Order order) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAmount(order.getShippingAmount())
                .taxAmount(order.getTaxAmount())
                .build();
    }

    // Version 2.0 - Commented out for future implementation
    /*
    public CustomerMetrics getCustomerMetrics(Long customerId) {
        // Customer metrics and analytics
        // To be implemented in version 2.0
    }

    public CustomerSegment analyzeCustomerSegment(Long customerId) {
        // Customer segmentation analysis
        // To be implemented in version 2.0
    }

    public CustomerLifetimeValue calculateLifetimeValue(Long customerId) {
        // Calculate customer lifetime value
        // To be implemented in version 2.0
    }

    public CustomerPreferences getCustomerPreferences(Long customerId) {
        // Marketing preferences and opt-ins
        // To be implemented in version 2.0
    }
    */
}