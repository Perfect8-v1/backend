package com.perfect8.shop.controller;

import com.perfect8.shop.service.*;
import com.perfect8.shop.dto.*;
import com.perfect8.shop.model.OrderStatus;
import com.perfect8.shop.model.PaymentStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Admin Dashboard
 *
 * Provides comprehensive overview and management capabilities for administrators.
 *
 * Dashboard Overview:
 * GET    /api/shop/admin/dashboard              - Main dashboard overview
 * GET    /api/shop/admin/dashboard/summary      - Quick summary stats
 * GET    /api/shop/admin/dashboard/alerts       - System alerts & notifications
 *
 * Real-time Metrics:
 * GET    /api/shop/admin/metrics/realtime       - Real-time metrics
 * GET    /api/shop/admin/metrics/sales          - Sales metrics
 * GET    /api/shop/admin/metrics/customers      - Customer metrics
 * GET    /api/shop/admin/metrics/inventory      - Inventory metrics
 *
 * Analytics:
 * GET    /api/shop/admin/analytics/revenue      - Revenue analytics
 * GET    /api/shop/admin/analytics/products     - Product performance
 * GET    /api/shop/admin/analytics/geographic   - Geographic analytics
 * GET    /api/shop/admin/analytics/trends       - Trend analysis
 */
@RestController
@RequestMapping("/api/shop/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final AnalyticsService analyticsService;

    @Autowired
    public AdminDashboardController(
            DashboardService dashboardService,
            OrderService orderService,
            CustomerService customerService,
            ProductService productService,
            PaymentService paymentService,
            InventoryService inventoryService,
            AnalyticsService analyticsService) {

        this.dashboardService = dashboardService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.analyticsService = analyticsService;
    }

    // ============================================================================
    // DASHBOARD OVERVIEW ENDPOINTS
    // ============================================================================

    /**
     * Get comprehensive dashboard overview
     *
     * @param period Optional time period (today, week, month, year)
     * @return Complete dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardOverview(
            @RequestParam(defaultValue = "week") String period) {

        AdminDashboardResponse dashboard = dashboardService.getDashboardOverview(period);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get quick summary statistics
     *
     * @return Summary statistics for dashboard widgets
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get system alerts and notifications
     *
     * @param priority Optional priority filter (HIGH, MEDIUM, LOW)
     * @param limit Maximum number of alerts to return
     * @return List of system alerts
     */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<SystemAlertResponse>> getSystemAlerts(
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<SystemAlertResponse> alerts = dashboardService.getSystemAlerts(priority, limit);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get recent activities for dashboard feed
     *
     * @param limit Maximum number of activities to return
     * @return Recent system activities
     */
    @GetMapping("/dashboard/activities")
    public ResponseEntity<List<ActivityFeedResponse>> getRecentActivities(
            @RequestParam(defaultValue = "20") Integer limit) {

        List<ActivityFeedResponse> activities = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    /**
     * Get dashboard widgets configuration
     *
     * @return Available dashboard widgets and their data
     */
    @GetMapping("/dashboard/widgets")
    public ResponseEntity<Map<String, Object>> getDashboardWidgets() {
        Map<String, Object> widgets = dashboardService.getDashboardWidgets();
        return ResponseEntity.ok(widgets);
    }

    // ============================================================================
    // REAL-TIME METRICS ENDPOINTS
    // ============================================================================

    /**
     * Get real-time metrics for live dashboard
     *
     * @return Real-time system metrics
     */
    @GetMapping("/metrics/realtime")
    public ResponseEntity<RealtimeMetricsResponse> getRealtimeMetrics() {
        RealtimeMetricsResponse metrics = dashboardService.getRealtimeMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get sales metrics
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param granularity Data granularity (hour, day, week, month)
     * @return Sales metrics data
     */
    @GetMapping("/metrics/sales")
    public ResponseEntity<SalesMetricsResponse> getSalesMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String granularity) {

        SalesMetricsResponse metrics = analyticsService.getSalesMetrics(startDate, endDate, granularity);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get customer metrics
     *
     * @param period Time period for analysis
     * @return Customer acquisition and behavior metrics
     */
    @GetMapping("/metrics/customers")
    public ResponseEntity<CustomerMetricsResponse> getCustomerMetrics(
            @RequestParam(defaultValue = "month") String period) {

        CustomerMetricsResponse metrics = analyticsService.getCustomerMetrics(period);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get inventory metrics
     *
     * @return Inventory status and alerts
     */
    @GetMapping("/metrics/inventory")
    public ResponseEntity<InventoryMetricsResponse> getInventoryMetrics() {
        InventoryMetricsResponse metrics = inventoryService.getInventoryMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get payment metrics
     *
     * @param days Number of days back to analyze
     * @return Payment processing metrics
     */
    @GetMapping("/metrics/payments")
    public ResponseEntity<PaymentMetricsResponse> getPaymentMetrics(
            @RequestParam(defaultValue = "30") Integer days) {

        PaymentMetricsResponse metrics = paymentService.getPaymentMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    // ============================================================================
    // ANALYTICS ENDPOINTS
    // ============================================================================

    /**
     * Get comprehensive revenue analytics
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param breakdown Breakdown type (daily, weekly, monthly)
     * @return Revenue analytics data
     */
    @GetMapping("/analytics/revenue")
    public ResponseEntity<RevenueAnalyticsResponse> getRevenueAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "daily") String breakdown) {

        RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics(startDate, endDate, breakdown);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get product performance analytics
     *
     * @param period Analysis period
     * @param metric Metric to analyze (sales, revenue, views)
     * @param limit Number of top products to include
     * @return Product performance data
     */
    @GetMapping("/analytics/products")
    public ResponseEntity<ProductAnalyticsResponse> getProductAnalytics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "sales") String metric,
            @RequestParam(defaultValue = "10") Integer limit) {

        ProductAnalyticsResponse analytics = analyticsService.getProductAnalytics(period, metric, limit);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get geographic analytics (sales by location)
     *
     * @param period Analysis period
     * @return Geographic distribution of sales
     */
    @GetMapping("/analytics/geographic")
    public ResponseEntity<GeographicAnalyticsResponse> getGeographicAnalytics(
            @RequestParam(defaultValue = "month") String period) {

        GeographicAnalyticsResponse analytics = analyticsService.getGeographicAnalytics(period);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get trend analysis
     *
     * @param metric Metric to analyze (orders, revenue, customers)
     * @param days Number of days to include in analysis
     * @return Trend analysis data
     */
    @GetMapping("/analytics/trends")
    public ResponseEntity<TrendAnalysisResponse> getTrendAnalysis(
            @RequestParam(defaultValue = "revenue") String metric,
            @RequestParam(defaultValue = "90") Integer days) {

        TrendAnalysisResponse trends = analyticsService.getTrendAnalysis(metric, days);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get cohort analysis for customer retention
     *
     * @param months Number of months to include
     * @return Cohort analysis data
     */
    @GetMapping("/analytics/cohort")
    public ResponseEntity<CohortAnalysisResponse> getCohortAnalysis(
            @RequestParam(defaultValue = "12") Integer months) {

        CohortAnalysisResponse cohort = analyticsService.getCohortAnalysis(months);
        return ResponseEntity.ok(cohort);
    }

    // ============================================================================
    // OPERATIONAL ENDPOINTS
    // ============================================================================

    /**
     * Get orders requiring attention (pending, issues, etc.)
     *
     * @param limit Maximum number of orders to return
     * @return Orders requiring admin attention
     */
    @GetMapping("/operations/orders-attention")
    public ResponseEntity<List<OrderAttentionResponse>> getOrdersRequiringAttention(
            @RequestParam(defaultValue = "20") Integer limit) {

        List<OrderAttentionResponse> orders = orderService.getOrdersRequiringAttention(limit);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get low stock alerts
     *
     * @param threshold Stock threshold for alerts
     * @param limit Maximum number of products to return
     * @return Products with low stock
     */
    @GetMapping("/operations/low-stock")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @RequestParam(defaultValue = "50") Integer limit) {

        List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlerts(threshold, limit);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get recent customer registrations
     *
     * @param days Number of days back to look
     * @param limit Maximum number of customers to return
     * @return Recent customer registrations
     */
    @GetMapping("/operations/new-customers")
    public ResponseEntity<List<NewCustomerResponse>> getNewCustomers(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "20") Integer limit) {

        List<NewCustomerResponse> customers = customerService.getNewCustomers(days, limit);
        return ResponseEntity.ok(customers);
    }

    /**
     * Get payment issues requiring attention
     *
     * @param limit Maximum number of issues to return
     * @return Payment issues
     */
    @GetMapping("/operations/payment-issues")
    public ResponseEntity<List<PaymentIssueResponse>> getPaymentIssues(
            @RequestParam(defaultValue = "10") Integer limit) {

        List<PaymentIssueResponse> issues = paymentService.getPaymentIssues(limit);
        return ResponseEntity.ok(issues);
    }

    // ============================================================================
    // PERFORMANCE ENDPOINTS
    // ============================================================================

    /**
     * Get system performance metrics
     *
     * @return System performance data
     */
    @GetMapping("/performance/system")
    public ResponseEntity<SystemPerformanceResponse> getSystemPerformance() {
        SystemPerformanceResponse performance = dashboardService.getSystemPerformance();
        return ResponseEntity.ok(performance);
    }

    /**
     * Get API performance metrics
     *
     * @param hours Number of hours back to analyze
     * @return API performance metrics
     */
    @GetMapping("/performance/api")
    public ResponseEntity<ApiPerformanceResponse> getApiPerformance(
            @RequestParam(defaultValue = "24") Integer hours) {

        ApiPerformanceResponse performance = dashboardService.getApiPerformance(hours);
        return ResponseEntity.ok(performance);
    }

    /**
     * Get database performance metrics
     *
     * @return Database performance data
     */
    @GetMapping("/performance/database")
    public ResponseEntity<DatabasePerformanceResponse> getDatabasePerformance() {
        DatabasePerformanceResponse performance = dashboardService.getDatabasePerformance();
        return ResponseEntity.ok(performance);
    }

    // ============================================================================
    // REPORTS ENDPOINTS
    // ============================================================================

    /**
     * Get executive summary report
     *
     * @param period Report period (week, month, quarter, year)
     * @return Executive summary data
     */
    @GetMapping("/reports/executive-summary")
    public ResponseEntity<ExecutiveSummaryResponse> getExecutiveSummary(
            @RequestParam(defaultValue = "month") String period) {

        ExecutiveSummaryResponse summary = analyticsService.getExecutiveSummary(period);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get sales report
     *
     * @param startDate Report start date
     * @param endDate Report end date
     * @param groupBy Grouping (day, week, month)
     * @return Sales report data
     */
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {

        SalesReportResponse report = analyticsService.getSalesReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(report);
    }

    /**
     * Get inventory report
     *
     * @param includeInactive Whether to include inactive products
     * @return Inventory report data
     */
    @GetMapping("/reports/inventory")
    public ResponseEntity<InventoryReportResponse> getInventoryReport(
            @RequestParam(defaultValue = "false") Boolean includeInactive) {

        InventoryReportResponse report = inventoryService.getInventoryReport(includeInactive);
        return ResponseEntity.ok(report);
    }

    /**
     * Get customer report
     *
     * @param period Analysis period
     * @param segmentation Customer segmentation criteria
     * @return Customer report data
     */
    @GetMapping("/reports/customers")
    public ResponseEntity<CustomerReportResponse> getCustomerReport(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "tier") String segmentation) {

        CustomerReportResponse report = customerService.getCustomerReport(period, segmentation);
        return ResponseEntity.ok(report);
    }

    // DTO Classes for responses
    public static class SystemAlertResponse {
        private String id;
        private String type;
        private String priority;
        private String title;
        private String message;
        private String category;
        private LocalDateTime timestamp;
        private Boolean acknowledged;
        private String actionUrl;

        // Constructors, getters, setters
        public SystemAlertResponse() {}

        public SystemAlertResponse(String id, String type, String priority, String title, String message,
                                   String category, LocalDateTime timestamp, Boolean acknowledged, String actionUrl) {
            this.id = id;
            this.type = type;
            this.priority = priority;
            this.title = title;
            this.message = message;
            this.category = category;
            this.timestamp = timestamp;
            this.acknowledged = acknowledged;
            this.actionUrl = actionUrl;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Boolean getAcknowledged() { return acknowledged; }
        public void setAcknowledged(Boolean acknowledged) { this.acknowledged = acknowledged; }

        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    }

    public static class ActivityFeedResponse {
        private String id;
        private String type;
        private String description;
        private String actor;
        private String target;
        private LocalDateTime timestamp;
        private String icon;
        private String severity;

        // Constructors, getters, setters
        public ActivityFeedResponse() {}

        public ActivityFeedResponse(String id, String type, String description, String actor, String target,
                                    LocalDateTime timestamp, String icon, String severity) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.actor = actor;
            this.target = target;
            this.timestamp = timestamp;
            this.icon = icon;
            this.severity = severity;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class RealtimeMetricsResponse {
        private Integer onlineCustomers;
        private Integer activeOrders;
        private BigDecimal todayRevenue;
        private Integer todayOrders;
        private Double systemLoad;
        private Integer pendingPayments;
        private Integer lowStockItems;
        private LocalDateTime lastUpdated;

        // Constructors, getters, setters
        public RealtimeMetricsResponse() {}

        public RealtimeMetricsResponse(Integer onlineCustomers, Integer activeOrders, BigDecimal todayRevenue,
                                       Integer todayOrders, Double systemLoad, Integer pendingPayments,
                                       Integer lowStockItems, LocalDateTime lastUpdated) {
            this.onlineCustomers = onlineCustomers;
            this.activeOrders = activeOrders;
            this.todayRevenue = todayRevenue;
            this.todayOrders = todayOrders;
            this.systemLoad = systemLoad;
            this.pendingPayments = pendingPayments;
            this.lowStockItems = lowStockItems;
            this.lastUpdated = lastUpdated;
        }

        // Getters and setters
        public Integer getOnlineCustomers() { return onlineCustomers; }
        public void setOnlineCustomers(Integer onlineCustomers) { this.onlineCustomers = onlineCustomers; }

        public Integer getActiveOrders() { return activeOrders; }
        public void setActiveOrders(Integer activeOrders) { this.activeOrders = activeOrders; }

        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

        public Integer getTodayOrders() { return todayOrders; }
        public void setTodayOrders(Integer todayOrders) { this.todayOrders = todayOrders; }

        public Double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(Double systemLoad) { this.systemLoad = systemLoad; }

        public Integer getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(Integer pendingPayments) { this.pendingPayments = pendingPayments; }

        public Integer getLowStockItems() { return lowStockItems; }
        public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}