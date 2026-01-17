package com.temple.service;

import com.temple.entity.EmailSubscription;
import com.temple.entity.SubscriptionLog;
import com.temple.repository.EmailSubscriptionRepository;
import com.temple.repository.SubscriptionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NewsletterSubscriptionService {

    @Autowired
    private EmailSubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionLogRepository logRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Subscribe a new user to the newsletter
     * @return Subscription object or null if email already exists
     */
    @Transactional
    public EmailSubscription subscribe(String email, String fullName, String phone) {
        try {
            // Check if email already exists
            Optional<EmailSubscription> existing = subscriptionRepository.findByEmail(email);
            if (existing.isPresent()) {
                EmailSubscription sub = existing.get();
                if (sub.getIsActive()) {
                    log.warn("SUBSCRIPTION_ALREADY_EXISTS | email={}", email);
                    return null; // Already subscribed
                } else {
                    // Reactivate if previously unsubscribed
                    sub.setIsActive(true);
                    sub.setSubscribedAt(LocalDateTime.now());
                    sub.setUnsubscribedAt(null);
                    EmailSubscription updated = subscriptionRepository.save(sub);

                    logAction(updated, "SUBSCRIBE", "User reactivated subscription");
                    emailService.sendSubscriptionConfirmation(updated);

                    log.info("SUBSCRIPTION_REACTIVATED | email={}", email);
                    return updated;
                }
            }

            // Create new subscription
            EmailSubscription subscription = EmailSubscription.builder()
                    .email(email)
                    .fullName(fullName)
                    .phone(phone)
                    .isActive(true)
                    .preferences("{\"frequency\": \"weekly\", \"interests\": [\"events\", \"news\"]}")
                    .build();

            EmailSubscription saved = subscriptionRepository.save(subscription);

            // Log the subscription
            logAction(saved, "SUBSCRIBE", "New subscription created");

            // Send confirmation email
            emailService.sendSubscriptionConfirmation(saved);

            log.info("SUBSCRIPTION_CREATED | email={} | name={}", email, fullName);
            return saved;

        } catch (Exception e) {
            log.error("SUBSCRIPTION_FAILED | email={} | error={}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Unsubscribe a user from the newsletter
     */
    @Transactional
    public boolean unsubscribe(String email) {
        try {
            Optional<EmailSubscription> subscription = subscriptionRepository.findByEmail(email);
            if (subscription.isPresent()) {
                EmailSubscription sub = subscription.get();
                sub.setIsActive(false);
                sub.setUnsubscribedAt(LocalDateTime.now());
                subscriptionRepository.save(sub);

                logAction(sub, "UNSUBSCRIBE", "User unsubscribed from newsletter");

                log.info("SUBSCRIPTION_CANCELLED | email={}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("UNSUBSCRIBE_FAILED | email={} | error={}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Get all active subscriptions
     */
    public List<EmailSubscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findAllActiveSubscriptions();
    }

    /**
     * Get subscription by email
     */
    public Optional<EmailSubscription> getSubscriptionByEmail(String email) {
        return subscriptionRepository.findByEmail(email);
    }

    /**
     * Get subscription count
     */
    public long getActiveSubscriptionCount() {
        return subscriptionRepository.countActiveSubscriptions();
    }

    /**
     * Get subscription statistics
     */
    public Map<String, Object> getSubscriptionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveSubscriptions", getActiveSubscriptionCount());
        stats.put("totalSubscriptions", subscriptionRepository.count());
        stats.put("successfulEmailsSent", logRepository.countSuccessfulEmails());
        stats.put("failedEmails", logRepository.countFailedEmails());
        return stats;
    }

    /**
     * Get subscription logs for a specific subscription
     */
    public List<SubscriptionLog> getSubscriptionLogs(Long subscriptionId) {
        Optional<EmailSubscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription.isPresent()) {
            return logRepository.findBySubscription(subscription.get());
        }
        return List.of();
    }

    /**
     * Log subscription action
     */
    @Transactional
    private void logAction(EmailSubscription subscription, String actionType, String details) {
        SubscriptionLog log = SubscriptionLog.builder()
                .subscription(subscription)
                .actionType(actionType)
                .details(details)
                .build();
        logRepository.save(log);
    }

    /**
     * Scheduled task: Send weekly newsletter every Sunday at 2:00 AM IST (8:30 PM UTC Saturday)
     * Cron: 0 30 20 ? * SAT
     */
    @Scheduled(cron = "0 30 20 ? * SAT", zone = "UTC")
    @Transactional
    public void sendWeeklyNewsletter() {
        log.info("WEEKLY_NEWSLETTER_JOB_STARTED");

        try {
            List<EmailSubscription> activeSubscriptions = getAllActiveSubscriptions();
            log.info("SENDING_NEWSLETTERS | count={}", activeSubscriptions.size());

            int successCount = 0;
            int failureCount = 0;

            for (EmailSubscription subscription : activeSubscriptions) {
                try {
                    boolean sent = emailService.sendWeeklyNewsletter(subscription);
                    if (sent) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("NEWSLETTER_SEND_ERROR | email={} | error={}", subscription.getEmail(), e.getMessage());
                    failureCount++;
                }
            }

            log.info("WEEKLY_NEWSLETTER_JOB_COMPLETED | success={} | failed={}", successCount, failureCount);

        } catch (Exception e) {
            log.error("WEEKLY_NEWSLETTER_JOB_FAILED | error={}", e.getMessage());
        }
    }

    /**
     * Alternative: Send weekly newsletter at a fixed time (2:00 AM IST every Sunday)
     * Using initialDelay and fixedDelay instead of cron for better control
     * Uncomment if cron doesn't work in your timezone
     */
    // @Scheduled(cron = "0 0 2 ? * SUN", zone = "Asia/Kolkata")
    // public void sendWeeklyNewsletterIST() {
    //     sendWeeklyNewsletter();
    // }

    /**
     * Update subscription preferences
     */
    @Transactional
    public EmailSubscription updatePreferences(Long subscriptionId, String preferences) {
        Optional<EmailSubscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription.isPresent()) {
            EmailSubscription sub = subscription.get();
            sub.setPreferences(preferences);
            return subscriptionRepository.save(sub);
        }
        return null;
    }
}

