package com.temple.repository;

import com.temple.entity.EmailSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscription, Long> {

    Optional<EmailSubscription> findByEmailIgnoreCase(String email);

    Optional<EmailSubscription> findByUnsubscribeToken(String unsubscribeToken);

    @Query("SELECT es FROM EmailSubscription es WHERE es.isActive = true ORDER BY es.subscribedAt DESC")
    List<EmailSubscription> findAllActiveSubscriptions();

    Page<EmailSubscription> findAllByOrderBySubscribedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(es) FROM EmailSubscription es WHERE es.isActive = true")
    long countActiveSubscriptions();
}
