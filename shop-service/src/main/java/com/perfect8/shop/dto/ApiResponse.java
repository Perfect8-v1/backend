package com.perfect8.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized API Response wrapper
 * Version 1.0 - Consistent response format across all endpoints
 *
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Success flag
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Response message
     */
    private String message;

    /**
     * Response data payload
     */
    private T data;

    /**
     * List of error messages
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * List of warning messages
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * HTTP status code
     */
    private Integer statusCode;

    /**
     * Request path that generated this response
     */
    private String path;

    /**
     * Response timestamp
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request ID for tracing
     */
    private String requestId;

    /**
     * Pagination info (if applicable)
     */
    private PaginationInfo pagination;

    // Constructors for backwards compatibility

    /**
     * Constructor for successful responses with data
     */
    public ApiResponse(String message, T data, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Constructor for error responses with error list
     */
    public ApiResponse(String message, List<String> errors, boolean success) {
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.warnings = new ArrayList<>();
    }

    // Static factory methods

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with single error
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with multiple errors
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors != null ? errors : new ArrayList<>())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a response with warning
     */
    public static <T> ApiResponse<T> successWithWarning(String message, T data, String warning) {
        List<String> warnings = new ArrayList<>();
        warnings.add(warning);
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .warnings(warnings)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a paginated response
     */
    public static <T> ApiResponse<T> paginated(String message, T data, PaginationInfo pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .pagination(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Helper methods

    /**
     * Add an error to the response
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.success = false;
    }

    /**
     * Add a warning to the response
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * Check if response has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if response has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Get error count
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Get warning count
     */
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }

    /**
     * Set HTTP status code and return this for chaining
     */
    public ApiResponse<T> withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Set request path and return this for chaining
     */
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Set request ID and return this for chaining
     */
    public ApiResponse<T> withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Pagination information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        /**
         * Create pagination info from Spring Page
         */
        public static PaginationInfo fromPage(org.springframework.data.domain.Page<?> page) {
            return PaginationInfo.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();
        }
    }
}