package com.temple.service;

import com.temple.config.AppProperties;
import com.temple.entity.EmailSubscription;
import com.temple.entity.SubscriptionLog;
import com.temple.repository.EmailSubscriptionRepository;
import com.temple.repository.SubscriptionLogRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class NewsletterSubscriptionService {

    /** Admin list row. Never includes the unsubscribe token. */
    public record SubscriberView(Long id, String email, String fullName, String status,
                                 LocalDateTime subscribedAt, LocalDateTime unsubscribedAt, LocalDateTime lastSentDate) {
        static SubscriberView from(EmailSubscription s) {
            return new SubscriberView(s.getId(), s.getEmail(), s.getFullName(),
                    Boolean.TRUE.equals(s.getIsActive()) ? "ACTIVE" : "UNSUBSCRIBED",
                    s.getSubscribedAt(), s.getUnsubscribedAt(), s.getLastSentAt());
        }
    }

    private final EmailSubscriptionRepository subscriptionRepository;
    private final SubscriptionLogRepository logRepository;
    private final EmailService emailService;
    private final AppProperties properties;

    public NewsletterSubscriptionService(EmailSubscriptionRepository subscriptionRepository,
                                         SubscriptionLogRepository logRepository,
                                         EmailService emailService, AppProperties properties) {
        this.subscriptionRepository = subscriptionRepository;
        this.logRepository = logRepository;
        this.emailService = emailService;
        this.properties = properties;
    }

    /** Idempotent: subscribing an address that is already active is a no-op; an unsubscribed one is re-activated. */
    public EmailSubscription subscribe(String rawEmail, String fullName, String phone) {
        String email = normalize(rawEmail);
        EmailSubscription sub = subscriptionRepository.findByEmailIgnoreCase(email).orElse(null);
        if (sub != null && Boolean.TRUE.equals(sub.getIsActive())) {
            return sub;
        }
        if (sub == null) {
            sub = EmailSubscription.builder()
                    .email(email)
                    .fullName(blankToNull(fullName))
                    .phone(blankToNull(phone))
                    .isActive(true)
                    .preferences("{\"frequency\": \"weekly\", \"interests\": [\"events\", \"news\"]}")
                    .build();
        } else {
            sub.setIsActive(true);
            sub.setSubscribedAt(LocalDateTime.now());
            sub.setUnsubscribedAt(null);
            if (blankToNull(fullName) != null) {
                sub.setFullName(fullName.trim());
            }
        }
        EmailSubscription saved = subscriptionRepository.save(sub);
        logAction(saved, "SUBSCRIBE", "Subscription activated");
        emailService.sendSubscriptionConfirmation(saved);
        return saved;
    }

    /** Silently ignores unknown addresses so the endpoint can't be used to probe who is subscribed. */
    public void unsubscribe(String rawEmail) {
        subscriptionRepository.findByEmailIgnoreCase(normalize(rawEmail)).ifPresent(this::deactivate);
    }

    public boolean unsubscribeByToken(String token) {
        return subscriptionRepository.findByUnsubscribeToken(token).map(s -> {
            deactivate(s);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<SubscriberView> list(int page, int size) {
        return subscriptionRepository.findAllByOrderBySubscribedAtDesc(PageRequest.of(page, size))
                .map(SubscriberView::from).getContent();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionStats() {
        long active = subscriptionRepository.countActiveSubscriptions();
        long total = subscriptionRepository.count();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSubscribers", total);
        stats.put("activeSubscribers", active);
        stats.put("unsubscribed", total - active);
        stats.put("successfulEmailsSent", logRepository.countSuccessfulEmails());
        stats.put("failedEmails", logRepository.countFailedEmails());
        stats.put("emailConfigured", emailService.isConfigured());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionLog> getSubscriptionLogs(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(logRepository::findBySubscription)
                .orElse(List.of());
    }

    public EmailSubscription updatePreferences(Long subscriptionId, String preferences) {
        EmailSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new com.temple.exception.NotFoundException("Subscription", subscriptionId));
        sub.setPreferences(preferences);
        return sub;
    }

    @Transactional(readOnly = true)
    public void sendTestEmail(String email) {
        emailService.sendTestEmail(normalize(email));
    }

    /** Weekly digest; runs only when NEWSLETTER_ENABLED=true (default: Sundays 02:00 IST). */
    @Scheduled(cron = "${app.newsletter.cron}", zone = "${app.newsletter.zone}")
    public void scheduledWeeklyNewsletter() {
        if (!properties.newsletter().enabled()) {
            return;
        }
        sendWeeklyNewsletter();
    }

    /** Sends the digest to all active subscribers now; returns {sent, failed}. */
    public Map<String, Integer> sendWeeklyNewsletter() {
        int sent = 0;
        int failed = 0;
        for (EmailSubscription subscription : subscriptionRepository.findAllActiveSubscriptions()) {
            try {
                if (emailService.sendWeeklyNewsletter(subscription)) {
                    subscription.setLastSentAt(LocalDateTime.now());
                    sent++;
                } else {
                    failed++;
                }
            } catch (RuntimeException e) {
                log.error("NEWSLETTER_SEND_ERROR | subscriptionId={} | error={}", subscription.getId(), e.getMessage());
                failed++;
            }
        }
        log.info("WEEKLY_NEWSLETTER_DONE | sent={} | failed={}", sent, failed);
        return Map.of("sent", sent, "failed", failed);
    }

    private void deactivate(EmailSubscription sub) {
        if (Boolean.TRUE.equals(sub.getIsActive())) {
            sub.setIsActive(false);
            sub.setUnsubscribedAt(LocalDateTime.now());
            logAction(sub, "UNSUBSCRIBE", "Unsubscribed");
        }
    }

    private void logAction(EmailSubscription subscription, String actionType, String details) {
        logRepository.save(SubscriptionLog.builder()
                .subscription(subscription)
                .actionType(actionType)
                .details(details)
                .build());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
