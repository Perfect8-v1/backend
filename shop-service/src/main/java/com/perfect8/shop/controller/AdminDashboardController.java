/*
package com.perfect8.shop.controller;

import com.perfect8.shop.service.*;
import com.perfect8.shop.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


REST Controller for Admin Dashboard XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

@Slf4j
@RestController
@RequestMapping("/api/shop/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardOverview(
            @RequestParam(defaultValue = "week") String period) {
        log.info("Getting dashboard overview for period: {}", period);

        try {
            // Create a basic dashboard response using available services
            AdminDashboardResponse dashboard = AdminDashboardResponse.builder()
                    .period(period)
                    .totalRevenue(java.math.BigDecimal.valueOf(50000)) // Placeholder
                    .totalOrders(100L) // Placeholder
                    .totalCustomers(customerService != null ? 250L : 0L) // Placeholder
                    .generatedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error getting dashboard overview: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        log.info("Getting dashboard summary");

        try {
            DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                    .totalOrders(100L)
                    .totalRevenue(java.math.BigDecimal.valueOf(25000))
                    .totalCustomers(250L)
                    .totalProducts(150L)
                    .generatedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting dashboard summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<SystemAlertResponse>> getSystemAlerts(
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Getting system alerts with priority: {} and limit: {}", priority, limit);

        try {
            // Return placeholder alerts for now
            List<SystemAlertResponse> alerts = List.of(
                    SystemAlertResponse.builder()
                            .id(1L)
                            .type("LOW_STOCK")
                            .priority("HIGH")
                            .message("5 products are running low on stock")
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error getting system alerts: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/activities")
    public ResponseEntity<List<ActivityFeedResponse>> getRecentActivities(
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("Getting recent activities with limit: {}", limit);

        try {
            // Return placeholder activities
            List<ActivityFeedResponse> activities = List.of(
                    ActivityFeedResponse.builder()
                            .id(1L)
                            .type("ORDER_CREATED")
                            .description("New order #1001 created")
                            .timestamp(LocalDateTime.now())
                            .build()
            );

            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            log.error("Error getting recent activities: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/widgets")
    public ResponseEntity<Map<String, Object>> getDashboardWidgets() {
        log.info("Getting dashboard widgets");

        try {
            Map<String, Object> widgets = new HashMap<>();
            widgets.put("totalOrders", 100);
            widgets.put("totalRevenue", 25000.00);
            widgets.put("totalCustomers", 250);
            widgets.put("lowStockItems", 5);

            return ResponseEntity.ok(widgets);
        } catch (Exception e) {
            log.error("Error getting dashboard widgets: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metrics/realtime")
    public ResponseEntity<RealtimeMetricsResponse> getRealtimeMetrics() {
        log.info("Getting realtime metrics");

        try {
            RealtimeMetricsResponse metrics = analyticsService.getRealTimeMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting realtime metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metrics/sales")
    public ResponseEntity<SalesMetricsResponse> getSalesMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        log.info("Getting sales metrics from {} to {} with granularity: {}", startDate, endDate, granularity);

        try {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

            SalesMetricsResponse metrics = analyticsService.getSalesMetrics(startDateTime, endDateTime, granularity);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting sales metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metrics/customers")
    public ResponseEntity<CustomerMetricsResponse> getCustomerMetrics(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Getting customer metrics for period: {}", period);

        try {
            CustomerMetricsResponse metrics = analyticsService.getCustomerMetrics(period);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting customer metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metrics/inventory")
    public ResponseEntity<InventoryMetricsResponse> getInventoryMetrics() {
        log.info("Getting inventory metrics");

        try {
            InventoryMetricsResponse metrics = inventoryService.getInventoryMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting inventory metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metrics/payments")
    public ResponseEntity<PaymentMetricsResponse> getPaymentMetrics(
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("Getting payment metrics for {} days", days);

        try {
            PaymentMetricsResponse metrics = paymentService.getPaymentMetrics(days);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting payment metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<RevenueAnalyticsResponse> getRevenueAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "daily") String breakdown) {
        log.info("Getting revenue analytics from {} to {} with breakdown: {}", startDate, endDate, breakdown);

        try {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

            RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics(startDateTime, endDateTime, breakdown);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting revenue analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/products")
    public ResponseEntity<ProductAnalyticsResponse> getProductAnalytics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "sales") String metric,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Getting product analytics for period: {}, metric: {}, limit: {}", period, metric, limit);

        try {
            LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
            LocalDateTime endDate = LocalDateTime.now();

            ProductAnalyticsResponse analytics = analyticsService.getProductAnalytics(startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting product analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/geographic")
    public ResponseEntity<GeographicAnalyticsResponse> getGeographicAnalytics(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Getting geographic analytics for period: {}", period);

        try {
            GeographicAnalyticsResponse analytics = analyticsService.getGeographicAnalytics(period);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting geographic analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/trends")
    public ResponseEntity<TrendAnalysisResponse> getTrendAnalysis(
            @RequestParam(defaultValue = "revenue") String metric,
            @RequestParam(defaultValue = "90") Integer days) {
        log.info("Getting trend analysis for metric: {}, days: {}", metric, days);

        try {
            TrendAnalysisResponse trends = analyticsService.getTrendAnalysis(metric, days);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            log.error("Error getting trend analysis: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/cohort")
    public ResponseEntity<CohortAnalysisResponse> getCohortAnalysis(
            @RequestParam(defaultValue = "12") Integer months) {
        log.info("Getting cohort analysis for {} months", months);

        try {
            CohortAnalysisResponse cohort = analyticsService.getCohortAnalysis(months);
            return ResponseEntity.ok(cohort);
        } catch (Exception e) {
            log.error("Error getting cohort analysis: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operations/orders-attention")
    public ResponseEntity<List<OrderAttentionResponse>> getOrdersRequiringAttention(
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("Getting orders requiring attention with limit: {}", limit);

        try {
            // Return placeholder data since OrderService doesn't have this method yet
            List<OrderAttentionResponse> orders = List.of(
                    OrderAttentionResponse.builder()
                            .orderId(1L)
                            .orderNumber("ORD-001")
                            .status("PENDING")
                            .customerName("John Doe")
                            .totalAmount(java.math.BigDecimal.valueOf(299.99))
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting orders requiring attention: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operations/low-stock")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @RequestParam(defaultValue = "50") Integer limit) {
        log.info("Getting low stock alerts with threshold: {}, limit: {}", threshold, limit);

        try {
            List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlerts(threshold, limit);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error getting low stock alerts: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operations/new-customers")
    public ResponseEntity<List<NewCustomerResponse>> getNewCustomers(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("Getting new customers for {} days with limit: {}", days, limit);

        try {
            // Return placeholder data since CustomerService doesn't have this method yet
            List<NewCustomerResponse> customers = List.of(
                    NewCustomerResponse.builder()
                            .customerId(1L)
                            .customerName("Jane Smith")
                            .email("jane@example.com")
                            .registrationDate(LocalDateTime.now())
                            .build()
            );

            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error getting new customers: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/operations/payment-issues")
    public ResponseEntity<List<PaymentIssueResponse>> getPaymentIssues(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Getting payment issues with limit: {}", limit);

        try {
            List<PaymentIssueResponse> issues = paymentService.getPaymentIssues(limit);
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            log.error("Error getting payment issues: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/performance/system")
    public ResponseEntity<SystemPerformanceResponse> getSystemPerformance() {
        log.info("Getting system performance metrics");

        try {
            SystemPerformanceResponse performance = analyticsService.getSystemPerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting system performance: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/performance/api")
    public ResponseEntity<ApiPerformanceResponse> getApiPerformance(
            @RequestParam(defaultValue = "24") Integer hours) {
        log.info("Getting API performance for {} hours", hours);

        try {
            ApiPerformanceResponse performance = analyticsService.getApiPerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting API performance: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/performance/database")
    public ResponseEntity<DatabasePerformanceResponse> getDatabasePerformance() {
        log.info("Getting database performance metrics");

        try {
            DatabasePerformanceResponse performance = analyticsService.getDatabasePerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting database performance: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/executive-summary")
    public ResponseEntity<ExecutiveSummaryResponse> getExecutiveSummary(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Getting executive summary for period: {}", period);

        try {
            ExecutiveSummaryResponse summary = analyticsService.getExecutiveSummary(period);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting executive summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        log.info("Getting sales report from {} to {} grouped by {}", startDate, endDate, groupBy);

        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            SalesReportResponse report = analyticsService.getSalesReport(startDateTime, endDateTime, groupBy);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting sales report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/inventory")
    public ResponseEntity<InventoryReportResponse> getInventoryReport(
            @RequestParam(defaultValue = "false") Boolean includeInactive) {
        log.info("Getting inventory report with includeInactive: {}", includeInactive);

        try {
            InventoryReportResponse report = inventoryService.getInventoryReport(includeInactive);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting inventory report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/customers")
    public ResponseEntity<CustomerReportResponse> getCustomerReport(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "tier") String segmentation) {
        log.info("Getting customer report for period: {}, segmentation: {}", period, segmentation);

        try {
            // Return placeholder data since CustomerService doesn't have this method yet
            CustomerReportResponse report = CustomerReportResponse.builder()
                    .period(period)
                    .segmentation(segmentation)
                    .totalCustomers(250L)
                    .activeCustomers(200L)
                    .newCustomers(25L)
                    .generatedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting customer report: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
*/