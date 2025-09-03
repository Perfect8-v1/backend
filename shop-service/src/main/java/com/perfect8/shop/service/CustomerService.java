package com.perfect8.shop.service;

import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.entity.Address;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.repository.AddressRepository;
import com.perfect8.shop.dto.CustomerDTO;
import com.perfect8.shop.dto.CustomerRegistrationDTO;
import com.perfect8.shop.dto.CustomerUpdateDTO;
import com.perfect8.shop.dto.AddressDTO;
import com.perfect8.shop.exception.CustomerNotFoundException;
import com.perfect8.shop.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register new customer - FIXED: Returns CustomerDTO
     */
    public CustomerDTO registerCustomer(CustomerRegistrationDTO registrationDTO) {
        log.info("Registering new customer: {}", registrationDTO.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + registrationDTO.getEmail());
        }

        // Create customer entity
        Customer customer = Customer.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .phone(registrationDTO.getPhoneNumber())
                .dateOfBirth(registrationDTO.getDateOfBirth() != null ?
                        registrationDTO.getDateOfBirth().atStartOfDay() : null)
                .isActive(true)
                .isEmailVerified(false)
                .acceptsMarketing(registrationDTO.getAcceptMarketingEmails() != null ?
                        registrationDTO.getAcceptMarketingEmails() : false)
                .registrationDate(LocalDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        // Convert to DTO and return
        return convertToDTO(savedCustomer);
    }

    /**
     * Update customer - FIXED: Accepts CustomerUpdateDTO
     */
    public Customer updateCustomer(Long customerId, CustomerUpdateDTO updateDTO) {
        log.info("Updating customer: {}", customerId);

        Customer customer = getCustomerById(customerId);

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
        if (updateDTO.getDateOfBirth() != null) {
            customer.setDateOfBirth(updateDTO.getDateOfBirth().atStartOfDay());
        }
        if (updateDTO.getSubscribeToNewsletter() != null) {
            customer.setAcceptsMarketing(updateDTO.getSubscribeToNewsletter());
        }
        if (updateDTO.getPreferredLanguage() != null) {
            customer.setPreferredLanguage(updateDTO.getPreferredLanguage());
        }

        // Handle password change if requested
        if (updateDTO.isPasswordChangeRequested() && updateDTO.getCurrentPassword() != null) {
            if (!passwordEncoder.matches(updateDTO.getCurrentPassword(), customer.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            customer.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully");

        return updatedCustomer;
    }

    /**
     * Get customer addresses - FIXED: Returns List<AddressDTO>
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getCustomerAddresses(Long customerId) {
        log.info("Getting addresses for customer: {}", customerId);

        Customer customer = getCustomerById(customerId);

        if (customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
            return new ArrayList<>();
        }

        return customer.getAddresses().stream()
                .map(this::convertAddressToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Add customer address - FIXED: Returns AddressDTO
     */
    public AddressDTO addCustomerAddress(Long customerId, AddressDTO addressDTO) {
        log.info("Adding address for customer: {}", customerId);

        Customer customer = getCustomerById(customerId);

        Address address = new Address();
        address.setCustomer(customer);
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setAddressLine2(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setDefaultAddress(addressDTO.isDefaultAddress());
        address.setAddressType(addressDTO.getAddressType());

        Address savedAddress = addressRepository.save(address);
        log.info("Address added successfully with ID: {}", savedAddress.getId());

        return convertAddressToDTO(savedAddress);
    }

    /**
     * Update customer address - FIXED: Returns AddressDTO
     */
    public AddressDTO updateCustomerAddress(Long customerId, Long addressId, AddressDTO addressDTO) {
        log.info("Updating address {} for customer: {}", addressId, customerId);

        Customer customer = getCustomerById(customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }

        // Update fields
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setAddressLine2(addressDTO.getAddressLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setUpdatedAt(LocalDateTime.now());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully");

        return convertAddressToDTO(updatedAddress);
    }

    /**
     * Set default address - FIXED: Returns AddressDTO
     */
    public AddressDTO setDefaultAddress(Long customerId, Long addressId) {
        log.info("Setting default address {} for customer: {}", addressId, customerId);

        Customer customer = getCustomerById(customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }

        // Remove default from other addresses
        customer.getAddresses().forEach(addr -> {
            if (addr.isDefaultAddress() && !addr.getId().equals(addressId)) {
                addr.setDefaultAddress(false);
                addressRepository.save(addr);
            }
        });

        // Set this as default
        address.setDefaultAddress(true);
        Address updatedAddress = addressRepository.save(address);

        log.info("Default address set successfully");

        return convertAddressToDTO(updatedAddress);
    }

    /**
     * Delete customer address
     */
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        log.info("Deleting address {} for customer: {}", addressId, customerId);

        Customer customer = getCustomerById(customerId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }

        addressRepository.delete(address);
        log.info("Address deleted successfully");
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    /**
     * Get all customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        try {
            return customerRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("Error checking email existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Delete customer (soft delete)
     */
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer: {}", customerId);

        Customer customer = getCustomerById(customerId);
        customer.setIsActive(false);
        customerRepository.save(customer);

        log.info("Customer deleted (soft delete) successfully");
    }

    /**
     * Toggle customer status
     */
    public Customer toggleCustomerStatus(Long customerId) {
        log.info("Toggling status for customer: {}", customerId);

        Customer customer = getCustomerById(customerId);
        customer.setIsActive(!customer.getIsActive());
        Customer updated = customerRepository.save(customer);

        log.info("Customer status toggled to: {}", updated.getIsActive());
        return updated;
    }

    /**
     * Search customers
     */
    public Page<Customer> searchCustomers(String name, String email, String phone, Pageable pageable) {
        log.info("Searching customers - name: {}, email: {}, phone: {}", name, email, phone);

        // Simple implementation - would need custom query in repository
        if (email != null && !email.isEmpty()) {
            return customerRepository.findByEmailContaining(email, pageable);
        }

        return customerRepository.findAll(pageable);
    }

    /**
     * Get recent customers
     */
    public List<Customer> getRecentCustomers(int limit) {
        log.info("Getting {} recent customers", limit);

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "registrationDate"));
        return customerRepository.findAll(pageRequest).getContent();
    }

    /**
     * Get new customers - for admin dashboard
     */
    public List<Object> getNewCustomers(Integer days, Integer limit) {
        log.info("Getting new customers from last {} days", days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "registrationDate"));

        List<Customer> customers = customerRepository.findByRegistrationDateAfter(since, pageRequest);

        // Convert to simple objects for dashboard
        return customers.stream()
                .map(c -> {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("id", c.getId());
                    customerInfo.put("name", c.getFullName());
                    customerInfo.put("email", c.getEmail());
                    customerInfo.put("registrationDate", c.getRegistrationDate());
                    return customerInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get customer report
     */
    public Object getCustomerReport(String period, String segmentation) {
        log.info("Getting customer report - period: {}, segmentation: {}", period, segmentation);

        Map<String, Object> report = new HashMap<>();
        report.put("totalCustomers", customerRepository.count());
        report.put("activeCustomers", customerRepository.countByIsActiveTrue());
        report.put("period", period);
        report.put("segmentation", segmentation);

        return report;
    }

    /**
     * Get customer statistics
     */
    @Transactional(readOnly = true)
    public Object getCustomerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerRepository.count());
        stats.put("activeCustomers", customerRepository.countByIsActiveTrue());
        stats.put("verifiedEmails", customerRepository.countByIsEmailVerifiedTrue());
        return stats;
    }

    // Helper methods

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhone());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setGender(customer.getGender());
        dto.setIsActive(customer.getIsActive());
        dto.setRegistrationDate(customer.getRegistrationDate());
        return dto;
    }

    private AddressDTO convertAddressToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .recipientName(address.getCustomer().getFullName())
                .streetAddress(address.getStreetAddress())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}