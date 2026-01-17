package com.temple.repository;

import com.temple.entity.EmailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscription, Long> {

    Optional<EmailSubscription> findByEmail(String email);

    List<EmailSubscription> findByIsActiveTrue();

    @Query("SELECT es FROM EmailSubscription es WHERE es.isActive = true ORDER BY es.subscribedAt DESC")
    List<EmailSubscription> findAllActiveSubscriptions();

    @Query("SELECT COUNT(es) FROM EmailSubscription es WHERE es.isActive = true")
    long countActiveSubscriptions();
}

