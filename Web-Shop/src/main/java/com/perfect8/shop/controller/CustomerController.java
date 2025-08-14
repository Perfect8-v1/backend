package com.perfect8.shop.controller;

import com.perfect8.shop.model.Customer;
import com.perfect8.shop.service.CustomerService;
import com.perfect8.shop.service.AuthenticationService;
import com.perfect8.shop.dto.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer management
 *
 * Public Endpoints:
 * POST   /api/shop/customers/register       - Register new customer
 * POST   /api/shop/customers/login          - Customer login
 * POST   /api/shop/customers/forgot-password - Request password reset
 * POST   /api/shop/customers/reset-password  - Reset password with token
 * GET    /api/shop/customers/verify/{token}  - Verify email address
 *
 * Customer Endpoints:
 * GET    /api/shop/customers/profile        - Get own profile
 * PUT    /api/shop/customers/profile        - Update own profile
 * DELETE /api/shop/customers/profile        - Delete own account
 * POST   /api/shop/customers/change-password - Change password
 * GET    /api/shop/customers/orders         - Get own orders
 * GET    /api/shop/customers/addresses      - Get saved addresses
 * POST   /api/shop/customers/addresses      - Add new address
 *
 * Admin Endpoints:
 * GET    /api/shop/customers                - Get all customers (Admin)
 * GET    /api/shop/customers/{id}           - Get customer by ID (Admin)
 * PUT    /api/shop/customers/{id}/status    - Update customer status (Admin)
 * GET    /api/shop/customers/stats          - Get customer statistics (Admin)
 */
@RestController
@RequestMapping("/api/shop/customers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class CustomerController {

    private final CustomerService customerService;
    private final AuthenticationService authenticationService;

    @Autowired
    public CustomerController(CustomerService customerService, AuthenticationService authenticationService) {
        this.customerService = customerService;
        this.authenticationService = authenticationService;
    }

    // ============================================================================
    // PUBLIC ENDPOINTS (No authentication required)
    // ============================================================================

    /**
     * Register new customer
     *
     * @param request Customer registration request
     * @return Registration response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponse> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {

        CustomerRegistrationResponse response = authenticationService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Customer login
     *
     * @param request Login credentials
     * @return Login response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<CustomerLoginResponse> loginCustomer(
            @Valid @RequestBody CustomerLoginRequest request) {

        CustomerLoginResponse response = authenticationService.loginCustomer(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset
     *
     * @param request Password reset request with email
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authenticationService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password reset email sent if email exists"));
    }

    /**
     * Reset password with token
     *
     * @param request Password reset request with token and new password
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        authenticationService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Verify email address
     *
     * @param token Email verification token
     * @return Verification response
     */
    @GetMapping("/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    /**
     * Check if email is available for registration
     *
     * @param email Email to check
     * @return Availability status
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean available = customerService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // ============================================================================
    // CUSTOMER ENDPOINTS (Authenticated customers only)
    // ============================================================================

    /**
     * Get customer's own profile
     *
     * @param authentication Customer authentication
     * @return Customer profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> getProfile(Authentication authentication) {
        String customerEmail = authentication.getName();
        CustomerProfileResponse profile = customerService.getCustomerProfile(customerEmail);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update customer's own profile
     *
     * @param request Profile update request
     * @param authentication Customer authentication
     * @return Updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @Valid @RequestBody CustomerProfileUpdateRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CustomerProfileResponse profile = customerService.updateCustomerProfile(customerEmail, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Change customer password
     *
     * @param request Password change request
     * @param authentication Customer authentication
     * @return Success message
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        customerService.changePassword(customerEmail, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Delete customer's own account
     *
     * @param request Account deletion request with password confirmation
     * @param authentication Customer authentication
     * @return Success message
     */
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        customerService.deleteCustomerAccount(customerEmail, request);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    /**
     * Get customer's order history
     *
     * @param authentication Customer authentication
     * @param pageable Pagination parameters
     * @return Customer's orders
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<CustomerOrderResponse>> getCustomerOrders(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        String customerEmail = authentication.getName();
        Page<CustomerOrderResponse> orders = customerService.getCustomerOrders(customerEmail, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get customer's saved addresses
     *
     * @param authentication Customer authentication
     * @return List of saved addresses
     */
    @GetMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CustomerAddressResponse>> getCustomerAddresses(
            Authentication authentication) {

        String customerEmail = authentication.getName();
        List<CustomerAddressResponse> addresses = customerService.getCustomerAddresses(customerEmail);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Add new address for customer
     *
     * @param request Address creation request
     * @param authentication Customer authentication
     * @return Created address
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerAddressResponse> addCustomerAddress(
            @Valid @RequestBody CustomerAddressRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CustomerAddressResponse address = customerService.addCustomerAddress(customerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    /**
     * Update customer address
     *
     * @param addressId Address ID
     * @param request Address update request
     * @param authentication Customer authentication
     * @return Updated address
     */
    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerAddressResponse> updateCustomerAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody CustomerAddressRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CustomerAddressResponse address = customerService.updateCustomerAddress(
                customerEmail, addressId, request);
        return ResponseEntity.ok(address);
    }

    /**
     * Delete customer address
     *
     * @param addressId Address ID
     * @param authentication Customer authentication
     * @return Success message
     */
    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> deleteCustomerAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        customerService.deleteCustomerAddress(customerEmail, addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    /**
     * Get customer's dashboard data
     *
     * @param authentication Customer authentication
     * @return Dashboard data (recent orders, stats, etc.)
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDashboardResponse> getCustomerDashboard(
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CustomerDashboardResponse dashboard = customerService.getCustomerDashboard(customerEmail);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Update customer preferences
     *
     * @param request Preferences update request
     * @param authentication Customer authentication
     * @return Updated preferences
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerPreferencesResponse> updatePreferences(
            @Valid @RequestBody CustomerPreferencesRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CustomerPreferencesResponse preferences = customerService.updateCustomerPreferences(
                customerEmail, request);
        return ResponseEntity.ok(preferences);
    }

    // ============================================================================
    // ADMIN ENDPOINTS (Admin access only)
    // ============================================================================

    /**
     * Get all customers (Admin only)
     *
     * @param pageable Pagination parameters
     * @param active Optional active status filter
     * @param emailVerified Optional email verification filter
     * @param customerTier Optional customer tier filter
     * @param registeredAfter Optional registration date filter
     * @return Paginated list of customers
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerAdminResponse>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(required = false) String customerTier,
            @RequestParam(required = false) LocalDateTime registeredAfter) {

        Page<CustomerAdminResponse> customers = customerService.findCustomers(
                pageable, active, emailVerified, customerTier, registeredAfter);
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer by ID (Admin only)
     *
     * @param id Customer ID
     * @return Customer details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerAdminDetailResponse> getCustomerById(@PathVariable Long id) {
        CustomerAdminDetailResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Search customers (Admin only)
     *
     * @param query Search query (name, email)
     * @param pageable Pagination parameters
     * @return Search results
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerAdminResponse>> searchCustomers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CustomerAdminResponse> customers = customerService.searchCustomers(query, pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Update customer status (Admin only)
     *
     * @param id Customer ID
     * @param request Status update request
     * @return Updated customer
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerAdminResponse> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {

        CustomerAdminResponse customer = customerService.updateCustomerStatus(id, request);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer statistics (Admin only)
     *
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Customer statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerStatisticsResponse> getCustomerStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        CustomerStatisticsResponse stats = customerService.getCustomerStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top customers by spending (Admin only)
     *
     * @param limit Number of top customers to return
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Top customers
     */
    @GetMapping("/top-customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopCustomerResponse>> getTopCustomers(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        List<TopCustomerResponse> topCustomers = customerService.getTopCustomers(limit, startDate, endDate);
        return ResponseEntity.ok(topCustomers);
    }

    /**
     * Get customer registrations over time (Admin only)
     *
     * @param days Number of days back to analyze
     * @return Daily registration metrics
     */
    @GetMapping("/registrations/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyRegistrationMetrics>> getDailyRegistrations(
            @RequestParam(defaultValue = "30") Integer days) {

        List<DailyRegistrationMetrics> metrics = customerService.getDailyRegistrations(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Export customers to CSV (Admin only)
     *
     * @param active Optional active status filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return CSV file response
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        byte[] csvData = customerService.exportCustomersToCSV(active, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=customers.csv")
                .body(csvData);
    }

    /**
     * Send marketing email to customers (Admin only)
     *
     * @param request Marketing email request
     * @return Success message
     */
    @PostMapping("/marketing/send-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendMarketingEmail(
            @Valid @RequestBody MarketingEmailRequest request) {

        customerService.sendMarketingEmail(request);
        return ResponseEntity.ok(Map.of("message", "Marketing email queued for sending"));
    }

    // DTO Classes for responses
    public static class DailyRegistrationMetrics {
        private String date;
        private Integer registrationCount;
        private Integer verifiedCount;
        private Double verificationRate;

        // Constructors, getters, setters
        public DailyRegistrationMetrics() {}

        public DailyRegistrationMetrics(String date, Integer registrationCount, Integer verifiedCount, Double verificationRate) {
            this.date = date;
            this.registrationCount = registrationCount;
            this.verifiedCount = verifiedCount;
            this.verificationRate = verificationRate;
        }

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Integer getRegistrationCount() { return registrationCount; }
        public void setRegistrationCount(Integer registrationCount) { this.registrationCount = registrationCount; }

        public Integer getVerifiedCount() { return verifiedCount; }
        public void setVerifiedCount(Integer verifiedCount) { this.verifiedCount = verifiedCount; }

        public Double getVerificationRate() { return verificationRate; }
        public void setVerificationRate(Double verificationRate) { this.verificationRate = verificationRate; }
    }

    public static class TopCustomerResponse {
        private Long customerId;
        private String customerName;
        private String email;
        private BigDecimal totalSpent;
        private Integer orderCount;
        private String customerTier;
        private LocalDateTime lastOrderDate;

        // Constructors, getters, setters
        public TopCustomerResponse() {}

        public TopCustomerResponse(Long customerId, String customerName, String email, BigDecimal totalSpent,
                                   Integer orderCount, String customerTier, LocalDateTime lastOrderDate) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.email = email;
            this.totalSpent = totalSpent;
            this.orderCount = orderCount;
            this.customerTier = customerTier;
            this.lastOrderDate = lastOrderDate;
        }

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

        public String getCustomerTier() { return customerTier; }
        public void setCustomerTier(String customerTier) { this.customerTier = customerTier; }

        public LocalDateTime getLastOrderDate() { return lastOrderDate; }
        public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    }
}