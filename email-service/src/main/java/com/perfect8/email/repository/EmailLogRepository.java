package com.perfect8.email.repository;

import com.perfect8.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for EmailLog entity
 * Version 1.0 - Core email logging functionality
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Find by emailId
    Optional<EmailLog> findByEmailId(String emailId);

    // Find by recipient
    List<EmailLog> findByRecipient(String recipient);

    // Find by status
    List<EmailLog> findByStatus(EmailLog.Status status);

    // Find by campaign
    List<EmailLog> findByCampaignId(String campaignId);

    // Find failed emails for retry
    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries")
    List<EmailLog> findFailedEmailsForRetry(@Param("maxRetries") int maxRetries);

    // Find emails sent within a date range
    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    List<EmailLog> findEmailsSentBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Find recent emails by recipient
    @Query("SELECT e FROM EmailLog e WHERE e.recipient = :recipient ORDER BY e.sentAt DESC")
    List<EmailLog> findRecentEmailsByRecipient(@Param("recipient") String recipient);

    // Count emails by status
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = :status")
    long countByStatus(@Param("status") EmailLog.Status status);

    // Find bounced emails
    @Query("SELECT e FROM EmailLog e WHERE e.status IN ('BOUNCED', 'SPAM_REPORTED', 'UNSUBSCRIBED')")
    List<EmailLog> findProblematicEmails();

    // Clean up old logs
    @Query("DELETE FROM EmailLog e WHERE e.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Check if email was recently sent to avoid duplicates
    @Query("SELECT COUNT(e) > 0 FROM EmailLog e WHERE e.recipient = :recipient " +
            "AND e.subject = :subject AND e.sentAt > :sinceTime")
    boolean existsByRecipientAndSubjectSince(@Param("recipient") String recipient,
                                             @Param("subject") String subject,
                                             @Param("sinceTime") LocalDateTime sinceTime);

    // Version 2.0 - Analytics queries (commented out)
    /*
    @Query("SELECT e.templateName, COUNT(e) as count FROM EmailLog e " +
           "WHERE e.status = 'OPENED' GROUP BY e.templateName")
    List<Object[]> getTemplateOpenRates();

    @Query("SELECT DATE(e.sentAt) as date, COUNT(e) as count FROM EmailLog e " +
           "WHERE e.sentAt >= :startDate GROUP BY DATE(e.sentAt)")
    List<Object[]> getDailyEmailVolume(@Param("startDate") LocalDateTime startDate);
    */
}