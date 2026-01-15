package com.perfect8.shop.controller;

import com.perfect8.shop.service.CustomerService;
import com.perfect8.shop.dto.CustomerDTO;
import com.perfect8.shop.dto.CustomerUpdateDTO;
import com.perfect8.shop.dto.AddressDTO;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.OrderDTO;
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
 * UPDATED (2025-12-28 - FAS 3):
 * - Removed JwtTokenProvider dependency (Gateway handles JWT)
 * - Removed helper methods (getCurrentCustomerId, getCurrentUserRole)
 * - Reads customer info directly from Gateway headers (X-Auth-User, X-Auth-Customer-Id, X-Auth-Role)
 * - Gateway validates JWT and adds headers before request reaches this service
 * 
 * Gateway Headers:
 * - X-Auth-User: username/email
 * - X-Auth-Customer-Id: customer ID (for customer logins)
 * - X-Auth-Role: user role (ROLE_ADMIN, ROLE_CUSTOMER, etc.)
 * 
 * Magnum Opus Principles:
 * - Returns DTOs not Entities
 * - Descriptive variable names (customerId not id)
 * - Sort field names match entity (createdDate)
 * - Trust Gateway for authentication (single point of JWT validation)
 * 
 * CORS hanteras globalt av WebConfig
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // ========== CUSTOMER PROFILE ENDPOINTS ==========

    /**
     * Get current customer profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getProfile(HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                // Fallback: lookup by email from X-Auth-User header
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Customer profile retrieved successfully",
                            customer,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO existingCustomer = customerService.getCustomerDTOByEmail(email);
                    CustomerDTO updatedCustomer = customerService.updateCustomer(existingCustomer.getCustomerId(), updateDTO);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Customer profile updated successfully",
                            updatedCustomer,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
     * Get all customers with pagination (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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
     * Search customers (admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> searchCustomers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerDTO> customers = customerService.searchCustomers(searchTerm, pageable);

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

    // ========== ADDRESS ENDPOINTS ==========

    /**
     * Get customer addresses
     */
    @GetMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getCustomerAddresses(HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    List<AddressDTO> addresses = customerService.getCustomerAddresses(customer.getCustomerId());
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Customer addresses retrieved successfully",
                            addresses,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
     * Add customer address
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> addCustomerAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    AddressDTO newAddress = customerService.addCustomerAddress(customer.getCustomerId(), addressDTO);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Address added successfully",
                            newAddress,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
            AddressDTO newAddress = customerService.addCustomerAddress(customerId, addressDTO);

            ApiResponse<AddressDTO> response = new ApiResponse<>(
                    "Address added successfully",
                    newAddress,
                    true
            );
            return ResponseEntity.ok(response);

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
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    AddressDTO updatedAddress = customerService.updateCustomerAddress(customer.getCustomerId(), addressId, addressDTO);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Address updated successfully",
                            updatedAddress,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    customerService.deleteCustomerAddress(customer.getCustomerId(), addressId);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Address deleted successfully",
                            "Address has been removed",
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
     */
    @PutMapping("/addresses/{addressId}/default")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable Long addressId,
            @RequestParam String addressType,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    AddressDTO defaultAddress = customerService.setDefaultAddress(customer.getCustomerId(), addressId, addressType);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Default address set successfully",
                            defaultAddress,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            if (customerIdHeader == null) {
                String email = request.getHeader("X-Auth-User");
                if (email != null) {
                    CustomerDTO customer = customerService.getCustomerDTOByEmail(email);
                    Pageable pageable = PageRequest.of(page, size);
                    Page<OrderDTO> orders = customerService.getCustomerOrders(customer.getCustomerId(), pageable);
                    return ResponseEntity.ok(new ApiResponse<>(
                            "Customer orders retrieved successfully",
                            orders,
                            true
                    ));
                }
                throw new RuntimeException("Unable to determine customer ID");
            }
            
            Long customerId = Long.parseLong(customerIdHeader);
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
}
