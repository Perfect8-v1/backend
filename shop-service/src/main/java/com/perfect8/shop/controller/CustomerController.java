package com.perfect8.shop.controller;

import com.perfect8.shop.service.CustomerService;
import com.perfect8.shop.dto.CustomerDTO;
import com.perfect8.shop.dto.CustomerRegistrationDTO;
import com.perfect8.shop.dto.CustomerUpdateDTO;
import com.perfect8.shop.dto.AddressDTO;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for customer management.
 * Version 1.0 - Core functionality only
 * FIXED: Returns DTOs not Entities!
 * FIXED: All @PathVariable parameters use descriptive names (customerId instead of id) - Magnum Opus principle
 * FIXED: Sort field names match entity convention (createdDate instead of createdAt) - Magnum Opus principle
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register new customer - Core functionality
     * Public endpoint for customer signup
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerDTO>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationDTO registrationDTO) {
        try {
            CustomerDTO newCustomer = customerService.registerCustomer(registrationDTO);

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer registered successfully",
                    newCustomer,
                    true
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to register customer: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get current customer profile - Core functionality
     * FIXED: Returns CustomerDTO not Customer entity
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getProfile(HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            CustomerDTO customer = customerService.getCustomerById(customerId);

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer profile retrieved successfully",
                    customer,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to retrieve customer profile: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update current customer profile - Core functionality
     * FIXED: Returns CustomerDTO not Customer entity
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateProfile(
            @Valid @RequestBody CustomerUpdateDTO updateDTO,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            CustomerDTO updatedCustomer = customerService.updateCustomer(customerId, updateDTO);

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer profile updated successfully",
                    updatedCustomer,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to update customer profile: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete current customer account - Core functionality
     * Soft delete for GDPR compliance
     */
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteAccount(HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            customerService.deleteCustomer(customerId);

            ApiResponse<String> response = new ApiResponse<>(
                    "Customer account deleted successfully",
                    "Account has been deactivated",
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "Failed to delete customer account: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Check if email exists - Core functionality
     * Public endpoint for registration validation
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = customerService.emailExists(email);

            ApiResponse<Boolean> response = new ApiResponse<>(
                    "Email check completed",
                    exists,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Boolean> errorResponse = new ApiResponse<>(
                    "Failed to check email: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ========== ADMIN ENDPOINTS - Core functionality for customer management ==========

    /**
     * Get customer by ID - Core functionality
     * FIXED: Returns CustomerDTO not Customer entity
     * FIXED: Changed @PathVariable from 'id' to 'customerId' (Magnum Opus principle)
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(@PathVariable Long customerId) {
        try {
            CustomerDTO customer = customerService.getCustomerById(customerId);

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer retrieved successfully",
                    customer,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to retrieve customer: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get all customers with pagination - Core functionality
     * FIXED: Returns Page<CustomerDTO> not Page<Customer>
     * FIXED: Changed default sortBy from 'createdAt' to 'createdDate' to match entity (Magnum Opus principle)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);

            ApiResponse<Page<CustomerDTO>> response = new ApiResponse<>(
                    "Customers retrieved successfully",
                    customers,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Page<CustomerDTO>> errorResponse = new ApiResponse<>(
                    "Failed to retrieve customers: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Search customers - Core functionality
     * FIXED: Returns Page<CustomerDTO> not Page<Customer>
     * FIXED: Changed default sortBy from 'createdAt' to 'createdDate' to match entity (Magnum Opus principle)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> searchCustomers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CustomerDTO> customers = customerService.searchCustomers(email, name, phone, pageable);

            ApiResponse<Page<CustomerDTO>> response = new ApiResponse<>(
                    "Customer search completed successfully",
                    customers,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Page<CustomerDTO>> errorResponse = new ApiResponse<>(
                    "Failed to search customers: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update customer by ID - Core functionality
     * FIXED: Returns CustomerDTO not Customer entity
     * FIXED: Changed @PathVariable from 'id' to 'customerId' (Magnum Opus principle)
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerUpdateDTO updateDTO) {
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomer(customerId, updateDTO);

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer updated successfully",
                    updatedCustomer,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to update customer: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Activate/Deactivate customer - Core functionality
     * FIXED: Returns CustomerDTO not Customer entity
     * FIXED: Changed @PathVariable from 'id' to 'customerId' (Magnum Opus principle)
     */
    @PutMapping("/{customerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> toggleCustomerStatus(@PathVariable Long customerId) {
        try {
            CustomerDTO customer = customerService.toggleCustomerStatus(customerId);
            String status = customer.isActive() ? "activated" : "deactivated";

            ApiResponse<CustomerDTO> response = new ApiResponse<>(
                    "Customer " + status + " successfully",
                    customer,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<CustomerDTO> errorResponse = new ApiResponse<>(
                    "Failed to toggle customer status: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get recent customers - Core functionality
     * FIXED: Returns List<CustomerDTO> not List<Customer>
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getRecentCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CustomerDTO> recentCustomers = customerService.getRecentCustomers(limit);

            ApiResponse<List<CustomerDTO>> response = new ApiResponse<>(
                    "Recent customers retrieved successfully",
                    recentCustomers,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<List<CustomerDTO>> errorResponse = new ApiResponse<>(
                    "Failed to retrieve recent customers: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ========== ADDRESS MANAGEMENT - Critical for shipping ==========

    /**
     * Get customer addresses - Core functionality
     * Critical for order placement
     */
    @GetMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getCustomerAddresses(HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            List<AddressDTO> addresses = customerService.getCustomerAddresses(customerId);

            ApiResponse<List<AddressDTO>> response = new ApiResponse<>(
                    "Customer addresses retrieved successfully",
                    addresses,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<List<AddressDTO>> errorResponse = new ApiResponse<>(
                    "Failed to retrieve customer addresses: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Add new address - Core functionality
     * Critical for shipping
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> addCustomerAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            AddressDTO newAddress = customerService.addCustomerAddress(customerId, addressDTO);

            ApiResponse<AddressDTO> response = new ApiResponse<>(
                    "Address added successfully",
                    newAddress,
                    true
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<AddressDTO> errorResponse = new ApiResponse<>(
                    "Failed to add address: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update customer address - Core functionality
     * Critical for correct deliveries
     */
    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> updateCustomerAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            AddressDTO updatedAddress = customerService.updateCustomerAddress(customerId, addressId, addressDTO);

            ApiResponse<AddressDTO> response = new ApiResponse<>(
                    "Address updated successfully",
                    updatedAddress,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<AddressDTO> errorResponse = new ApiResponse<>(
                    "Failed to update address: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete customer address - Core functionality
     */
    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteCustomerAddress(
            @PathVariable Long addressId,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            customerService.deleteCustomerAddress(customerId, addressId);

            ApiResponse<String> response = new ApiResponse<>(
                    "Address deleted successfully",
                    "Address has been removed",
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "Failed to delete address: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Set default address - Core functionality
     * Simplifies checkout process
     */
    @PutMapping("/addresses/{addressId}/default")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable Long addressId,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            AddressDTO defaultAddress = customerService.setDefaultAddress(customerId, addressId);

            ApiResponse<AddressDTO> response = new ApiResponse<>(
                    "Default address set successfully",
                    defaultAddress,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<AddressDTO> errorResponse = new ApiResponse<>(
                    "Failed to set default address: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ========== Helper methods ==========

    /**
     * Get customer ID from JWT token
     */
    private Long getCurrentCustomerId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getCustomerIdFromToken(token);
        }
        throw new RuntimeException("Unable to determine customer ID from token");
    }

    /**
     * Get user role from JWT token
     */
    private String getCurrentUserRole(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getRoleFromToken(token);
        }
        throw new RuntimeException("Unable to determine user role from token");
    }
}