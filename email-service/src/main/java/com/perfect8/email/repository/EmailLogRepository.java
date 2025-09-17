package com.perfect8.email.repository;

import com.perfect8.email.entity.EmailLog;
import com.perfect8.email.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Email Log Repository - Version 1.0
 * Handles persistence of email logs and tracking
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /**
     * Find top 100 most recent email logs ordered by creation date
     * Used by EmailService for recent email history
     */
    List<EmailLog> findTop100ByOrderByCreatedAtDesc();

    /**
     * Find email log by message ID
     */
    Optional<EmailLog> findByMessageId(String messageId);

    /**
     * Find all emails sent to a specific recipient
     */
    List<EmailLog> findByRecipientOrderByCreatedAtDesc(String recipientEmail);

    /**
     * Find recent emails sent to a recipient (limit 50)
     */
    List<EmailLog> findTop50ByRecipientOrderByCreatedAtDesc(String recipientEmail);

    /**
     * Find emails by status
     */
    List<EmailLog> findByStatus(EmailStatus status);

    /**
     * Find emails by status with limit
     */
    List<EmailLog> findTop100ByStatusOrderByCreatedAtDesc(EmailStatus status);

    /**
     * Find failed emails that need retry
     */
    @Query("SELECT el FROM EmailLog el WHERE el.status = :status AND el.retryCount < :maxRetries")
    List<EmailLog> findFailedEmailsForRetry(@Param("status") EmailStatus status,
                                            @Param("maxRetries") int maxRetries);

    /**
     * Find emails sent within a date range
     */
    List<EmailLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
                                                              LocalDateTime endDate);

    /**
     * Find emails by subject containing text
     */
    List<EmailLog> findBySubjectContainingIgnoreCaseOrderByCreatedAtDesc(String subjectKeyword);

    /**
     * Find emails related to a specific order
     */
    @Query("SELECT el FROM EmailLog el WHERE el.metadata LIKE CONCAT('%orderId\":', :orderId, ',%') ORDER BY el.createdAt DESC")
    List<EmailLog> findByOrderId(@Param("orderId") String orderId);

    /**
     * Count emails sent today
     */
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.createdAt >= :startOfDay")
    long countEmailsSentToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Count emails by status
     */
    long countByStatus(EmailStatus status);

    /**
     * Find pending emails for processing
     */
    @Query("SELECT el FROM EmailLog el WHERE el.status = 'PENDING' AND el.scheduledAt <= :now ORDER BY el.scheduledAt ASC")
    List<EmailLog> findPendingEmailsForProcessing(@Param("now") LocalDateTime now);

    /**
     * Find emails that were sent successfully in the last N hours
     */
    @Query("SELECT el FROM EmailLog el WHERE el.status = 'SENT' AND el.sentAt >= :since ORDER BY el.sentAt DESC")
    List<EmailLog> findRecentlySentEmails(@Param("since") LocalDateTime since);

    /**
     * Delete old email logs (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Check if email was recently sent to prevent duplicates
     */
    @Query("SELECT COUNT(el) > 0 FROM EmailLog el WHERE el.recipient = :recipient " +
            "AND el.subject = :subject AND el.createdAt >= :since")
    boolean existsByRecipientAndSubjectAndCreatedAtAfter(@Param("recipient") String recipient,
                                                         @Param("subject") String subject,
                                                         @Param("since") LocalDateTime since);
}