package com.perfect8.email.exception;

public class EmailException extends RuntimeException {

    private final ErrorCode errorCode;

    public EmailException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public EmailException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public EmailException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        GENERAL_ERROR("EMAIL_001", "General email error"),
        INVALID_RECIPIENT("EMAIL_002", "Invalid recipient email address"),
        TEMPLATE_NOT_FOUND("EMAIL_003", "Email template not found"),
        SMTP_ERROR("EMAIL_004", "SMTP server error"),
        AUTHENTICATION_ERROR("EMAIL_005", "Email authentication failed"),
        RATE_LIMIT_EXCEEDED("EMAIL_006", "Email rate limit exceeded"),
        ATTACHMENT_ERROR("EMAIL_007", "Email attachment error"),
        TEMPLATE_PROCESSING_ERROR("EMAIL_008", "Template processing error"),
        INVALID_CONFIGURATION("EMAIL_009", "Invalid email configuration"),
        BULK_EMAIL_ERROR("EMAIL_010", "Bulk email processing error");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}