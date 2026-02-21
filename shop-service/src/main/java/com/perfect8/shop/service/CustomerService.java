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
 * 
 * MAGNUM OPUS (2025-11-22):
 * - Boolean fields: active, emailVerified (NOT isActive, isEmailVerified)
 * - Lombok generates: isActive(), setActive() from field "active"
 * - Builder uses: .active(boolean), .emailVerified(boolean)
 * - Repository uses: countByActiveTrue(), findByEmailVerifiedTrue()
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

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerDTOByEmail(String email) {
        return convertToDTO(getCustomerByEmail(email));
    }

    @Transactional(readOnly = true)
    public boolean customerExistsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean customerExistsByUserId(Long userId) {
        return customerRepository.findByUserId(userId).isPresent();
    }

    // ==================== Customer Creation ====================

    @Transactional
    public CustomerDTO createCustomerProfile(Long userId, String email, String firstName, String lastName) {
        log.debug("Creating customer profile for userId: {}", userId);

        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }

        Customer customer = Customer.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .active(true)
                .emailVerified(false)
                .newsletterSubscribed(false)
                .marketingConsent(false)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer profile created with ID: {} for userId: {}", savedCustomer.getCustomerId(), userId);

        try {
            String fullName = savedCustomer.getFirstName() + " " + savedCustomer.getLastName();
            emailService.sendWelcomeEmail(savedCustomer.getEmail(), fullName);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", savedCustomer.getEmail(), e);
        }

        return convertToDTO(savedCustomer);
    }

    @Transactional
    public CustomerDTO createGuestCustomer(String email, String firstName, String lastName, String phone) {
        log.debug("Creating guest customer with email: {}", email);

        if (customerRepository.existsByEmail(email)) {
            Customer existing = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
            return convertToDTO(existing);
        }

        Customer customer = Customer.builder()
                .userId(null)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone) //TODO Check if it is phoneNumer or phone
                .active(true)
                .emailVerified(false)
                .newsletterSubscribed(false)
                .marketingConsent(false)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Guest customer created with ID: {}", savedCustomer.getCustomerId());

        return convertToDTO(savedCustomer);
    }

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

    @Transactional
    public CustomerDTO updateCustomer(Long customerId, CustomerUpdateDTO updateDTO) {
        log.debug("Updating customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (updateDTO.getFirstName() != null) {
            customer.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            customer.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhone() != null) { //TODO Number or not
            customer.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getPreferredLanguage() != null) {
            customer.setPreferredLanguage(updateDTO.getPreferredLanguage());
        }
        if (updateDTO.getPreferredCurrency() != null) {
            customer.setPreferredCurrency(updateDTO.getPreferredCurrency());
        }
        if (updateDTO.getNewsletterSubscribed() != null) {
            customer.setNewsletterSubscribed(updateDTO.getNewsletterSubscribed());
        }
        if (updateDTO.getMarketingConsent() != null) {
            customer.setMarketingConsent(updateDTO.getMarketingConsent());
        }

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer {} updated successfully", customerId);

        return convertToDTO(savedCustomer);
    }

    @Transactional
    public CustomerDTO deactivateCustomer(Long customerId) {
        log.debug("Deactivating customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setActive(false);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer {} deactivated", customerId);
        return convertToDTO(savedCustomer);
    }

    @Transactional
    public CustomerDTO reactivateCustomer(Long customerId) {
        log.debug("Reactivating customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setActive(true);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer {} reactivated", customerId);
        return convertToDTO(savedCustomer);
    }

    // ==================== Address Management ====================

    @Transactional
    public AddressDTO addCustomerAddress(Long customerId, AddressDTO addressDTO) {
        log.debug("Adding address for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address address = Address.builder()
                .customer(customer)
                .recipientName(addressDTO.getRecipientName())
                .phoneNumber(addressDTO.getPhoneNumber())
                .street(addressDTO.getStreetAddress())
                .apartment(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .addressType(addressDTO.getAddressType())
                .defaultAddress(addressDTO.isDefaultShipping() || addressDTO.isDefaultBilling())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address {} added for customer ID: {}", savedAddress.getAddressId(), customerId);

        return convertToAddressDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateCustomerAddress(Long customerId, Long addressId, AddressDTO addressDTO) {
        log.debug("Updating address {} for customer ID: {}", addressId, customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        if (addressDTO.getRecipientName() != null) {
            address.setRecipientName(addressDTO.getRecipientName());
        }
        if (addressDTO.getPhoneNumber() != null) {
            address.setPhoneNumber(addressDTO.getPhoneNumber());
        }
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

    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        log.debug("Deleting address {} for customer ID: {}", addressId, customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        addressRepository.delete(address);
        log.info("Address {} deleted for customer ID: {}", addressId, customerId);
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long customerId, Long addressId, String addressType) {
        log.debug("Setting default {} address {} for customer ID: {}", addressType, addressId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Address targetAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        if (!targetAddress.getCustomer().getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Address does not belong to customer");
        }

        customer.getAddresses().forEach(a -> {
            if (a.getAddressType().equals(addressType)) {
                a.setDefaultAddress(false);
                addressRepository.save(a);
            }
        });

        targetAddress.setDefaultAddress(true);
        targetAddress.setAddressType(addressType);
        addressRepository.save(targetAddress);

        log.info("Address {} set as default {} for customer ID: {}", addressId, addressType, customerId);

        return convertToAddressDTO(targetAddress);
    }

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

    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedDateDesc(customer, pageable);
        return orders.map(this::convertToOrderDTO);
    }

    // ==================== Admin Methods ====================

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> searchCustomers(String searchTerm, Pageable pageable) {
        log.debug("Searching customers with term: {}", searchTerm);
        return customerRepository.searchCustomers(searchTerm, pageable).map(this::convertToDTO);
    }

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

    private CustomerDTO convertToDTO(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Cannot convert null Customer to DTO");
        }

        List<AddressDTO> addressDTOs = customer.getAddresses() != null
                ? customer.getAddresses().stream()
                    .map(this::convertToAddressDTO)
                    .collect(Collectors.toList())
                : List.of();

        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .userId(customer.getUserId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhone())
                .active(customer.isActive())
                .emailVerified(customer.isEmailVerified())
                .newsletterSubscribed(customer.isNewsletterSubscribed())
                .marketingConsent(customer.isMarketingConsent())
                .preferredLanguage(customer.getPreferredLanguage())
                .preferredCurrency(customer.getPreferredCurrency())
                .createdDate(customer.getCreatedDate())
                .updatedDate(customer.getUpdatedDate())
                .lastLoginDate(customer.getLastLoginDate())
                .addresses(addressDTOs)
                .build();
    }

    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Cannot convert null Address to DTO");
        }

        String addressType = address.getAddressType() != null ? address.getAddressType() : "BOTH";
        boolean isDefault = address.isDefaultAddress();

        return AddressDTO.builder()
                .addressId(address.getAddressId())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .streetAddress(address.getStreet())
                .addressLine2(address.getApartment())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .addressType(addressType)
                .defaultShipping(isDefault && ("SHIPPING".equals(addressType) || "BOTH".equals(addressType)))
                .defaultBilling(isDefault && ("BILLING".equals(addressType) || "BOTH".equals(addressType)))
                .build();
    }

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

    @lombok.Data
    @lombok.Builder
    public static class CustomerStatsDTO {
        private long totalCustomers;
        private long activeCustomers;
        private long inactiveCustomers;
        private long verifiedCustomers;
    }
}
