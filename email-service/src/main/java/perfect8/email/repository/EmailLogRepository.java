package perfect8.email.repository;

import perfect8.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Optional<EmailLog> findByEmailId(String emailId);

    List<EmailLog> findByRecipient(String recipient);

    List<EmailLog> findByStatus(EmailLog.Status status);

    List<EmailLog> findByCampaignId(String campaignId);

    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    List<EmailLog> findEmailsSentBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries")
    List<EmailLog> findFailedEmailsForRetry(@Param("maxRetries") int maxRetries);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.recipient = :recipient AND e.sentAt > :since")
    long countEmailsSentToRecipientSince(@Param("recipient") String recipient,
                                         @Param("since") LocalDateTime since);

    @Query("SELECT e.status, COUNT(e) FROM EmailLog e WHERE e.campaignId = :campaignId GROUP BY e.status")
    List<Object[]> getEmailStatusCountsByCampaign(@Param("campaignId") String campaignId);
}