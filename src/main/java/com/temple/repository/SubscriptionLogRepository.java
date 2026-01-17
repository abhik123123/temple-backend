package com.temple.repository;

import com.temple.entity.SubscriptionLog;
import com.temple.entity.EmailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionLogRepository extends JpaRepository<SubscriptionLog, Long> {

    List<SubscriptionLog> findBySubscription(EmailSubscription subscription);

    @Query("SELECT sl FROM SubscriptionLog sl WHERE sl.actionType IN ('EMAIL_SENT', 'EMAIL_FAILED') ORDER BY sl.createdAt DESC")
    List<SubscriptionLog> findAllEmailLogs();

    @Query("SELECT COUNT(sl) FROM SubscriptionLog sl WHERE sl.actionType = 'EMAIL_SENT'")
    long countSuccessfulEmails();

    @Query("SELECT COUNT(sl) FROM SubscriptionLog sl WHERE sl.actionType = 'EMAIL_FAILED'")
    long countFailedEmails();
}

