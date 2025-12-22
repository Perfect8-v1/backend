package com.perfect8.shop.controller;

import com.perfect8.shop.service.CustomerService;
import com.perfect8.shop.dto.CustomerDTO;
import com.perfect8.shop.dto.CustomerUpdateDTO;
import com.perfect8.shop.dto.AddressDTO;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.OrderDTO;
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
 * REST controller for customer management - Version 1.0
 * 
 * UPDATED (2025-11-20):
 * - Removed auth endpoints (moved to admin-service)
 * - Removed registerCustomer, deleteCustomer, emailExists
 * - Removed searchCustomers, toggleCustomerStatus, getRecentCustomers
 * - Fixed setDefaultAddress signature (3 parameters)
 * 
 * Remaining endpoints:
 * - Customer profile management
 * - Address management
 * - Order history viewing
 * - Admin customer queries
 * 
 * Magnum Opus Principles:
 * - Returns DTOs not Entities
 * - Descriptive variable names (customerId not id)
 * - Sort field names match entity (createdDate)
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ========== CUSTOMER PROFILE ENDPOINTS ==========

    /**
     * Get current customer profile
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
     * Update current customer profile
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
     * Check if email exists (for validation)
     * Uses customerExistsByEmail from CustomerService
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = customerService.customerExistsByEmail(email);

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

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get customer by ID (admin only)
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
     * Get customer by email (admin only)
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerByEmail(@PathVariable String email) {
        try {
            CustomerDTO customer = customerService.getCustomerDTOByEmail(email);

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
     * Get all customers (admin only, paginated)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
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

    // ========== ADDRESS MANAGEMENT ==========

    /**
     * Get customer addresses
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
     * Add new address
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
     * Update customer address
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
     * Delete customer address
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
     * Set default address
     * FIXED: Now includes addressType parameter as required by CustomerService
     */
    @PutMapping("/addresses/{addressId}/default")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable Long addressId,
            @RequestParam String addressType,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);
            AddressDTO defaultAddress = customerService.setDefaultAddress(customerId, addressId, addressType);

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

    // ========== ORDER HISTORY ==========

    /**
     * Get customer order history
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getCustomerOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long customerId = getCurrentCustomerId(request);
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = customerService.getCustomerOrders(customerId, pageable);

            ApiResponse<Page<OrderDTO>> response = new ApiResponse<>(
                    "Customer orders retrieved successfully",
                    orders,
                    true
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Page<OrderDTO>> errorResponse = new ApiResponse<>(
                    "Failed to retrieve customer orders: " + e.getMessage(),
                    null,
                    false
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Get customer ID from JWT token
     * FIXED (2025-12-22): First try customerId from token, then lookup by email
     * This supports both admin-service tokens (email only) and shop tokens (with customerId)
     */
    private Long getCurrentCustomerId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // First try to get customerId directly from token
            Long customerId = jwtTokenProvider.getCustomerIdFromToken(token);
            if (customerId != null) {
                return customerId;
            }

            // Fallback: lookup customer by email from token
            String email = jwtTokenProvider.getEmailFromToken(token);
            if (email != null) {
                try {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    if (customer != null) {
                        return customer.getCustomerId();
                    }
                } catch (Exception e) {
                    // Customer not found by email - will throw below
                }
            }
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
