package com.temple.controller;

import com.temple.entity.EmailSubscription;
import com.temple.entity.SubscriptionLog;
import com.temple.security.AdminOnly;
import com.temple.service.NewsletterSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/subscriptions")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class NewsletterSubscriptionController {

    @Autowired
    private NewsletterSubscriptionService subscriptionService;

    /**
     * Subscribe a new user to the newsletter
     * POST /subscriptions/subscribe
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String fullName = request.get("fullName");
            String phone = request.getOrDefault("phone", "");

            // Validation
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
            }

            // Email validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            }

            EmailSubscription subscription = subscriptionService.subscribe(email, fullName, phone);

            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email is already subscribed"));
            }

            log.info("SUBSCRIPTION_API_SUCCESS | email={}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Subscription successful! Check your email for confirmation.",
                    "subscription", subscription
            ));

        } catch (Exception e) {
            log.error("SUBSCRIPTION_API_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Subscription failed. Please try again later."));
        }
    }

    /**
     * Unsubscribe from the newsletter
     * POST /subscriptions/unsubscribe
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            boolean success = subscriptionService.unsubscribe(email);

            if (success) {
                log.info("UNSUBSCRIBE_API_SUCCESS | email={}", email);
                return ResponseEntity.ok(Map.of("message", "You have been unsubscribed successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("UNSUBSCRIBE_API_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unsubscribe failed. Please try again later."));
        }
    }

    /**
     * Get all active subscriptions (Admin only)
     * GET /subscriptions/admin/all
     */
    @AdminOnly
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllSubscriptions() {
        try {
            List<EmailSubscription> subscriptions = subscriptionService.getAllActiveSubscriptions();
            log.info("GET_ALL_SUBSCRIPTIONS_API | count={}", subscriptions.size());
            return ResponseEntity.ok(Map.of(
                    "count", subscriptions.size(),
                    "subscriptions", subscriptions
            ));
        } catch (Exception e) {
            log.error("GET_ALL_SUBSCRIPTIONS_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch subscriptions"));
        }
    }

    /**
     * Get subscription statistics (Admin only)
     * GET /subscriptions/admin/stats
     */
    @AdminOnly
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getSubscriptionStats() {
        try {
            Map<String, Object> stats = subscriptionService.getSubscriptionStats();
            log.info("GET_SUBSCRIPTION_STATS_API | stats={}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("GET_SUBSCRIPTION_STATS_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Get subscription by email
     * GET /subscriptions/{email}
     */
    @GetMapping("/{email}")
    public ResponseEntity<?> getSubscription(@PathVariable String email) {
        try {
            Optional<EmailSubscription> subscription = subscriptionService.getSubscriptionByEmail(email);
            if (subscription.isPresent()) {
                return ResponseEntity.ok(subscription.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("GET_SUBSCRIPTION_FAILED | email={} | error={}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch subscription"));
        }
    }

    /**
     * Get subscription logs for a subscription (Admin only)
     * GET /subscriptions/admin/logs/{subscriptionId}
     */
    @AdminOnly
    @GetMapping("/admin/logs/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionLogs(@PathVariable Long subscriptionId) {
        try {
            List<SubscriptionLog> logs = subscriptionService.getSubscriptionLogs(subscriptionId);
            return ResponseEntity.ok(Map.of(
                    "count", logs.size(),
                    "logs", logs
            ));
        } catch (Exception e) {
            log.error("GET_SUBSCRIPTION_LOGS_FAILED | id={} | error={}", subscriptionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch logs"));
        }
    }

    /**
     * Update subscription preferences (User can update their own, Admin can update any)
     * PUT /subscriptions/{subscriptionId}/preferences
     */
    @PutMapping("/{subscriptionId}/preferences")
    public ResponseEntity<?> updatePreferences(
            @PathVariable Long subscriptionId,
            @RequestBody Map<String, String> request) {
        try {
            String preferences = request.get("preferences");
            if (preferences == null || preferences.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Preferences are required"));
            }

            EmailSubscription updated = subscriptionService.updatePreferences(subscriptionId, preferences);
            if (updated != null) {
                log.info("UPDATE_PREFERENCES_SUCCESS | id={}", subscriptionId);
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("UPDATE_PREFERENCES_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update preferences"));
        }
    }

    /**
     * Manual trigger for weekly newsletter (Admin only) - for testing
     * POST /subscriptions/admin/send-newsletter
     */
    @AdminOnly
    @PostMapping("/admin/send-newsletter")
    public ResponseEntity<?> manualSendNewsletter() {
        try {
            log.info("MANUAL_NEWSLETTER_TRIGGER");
            subscriptionService.sendWeeklyNewsletter();
            return ResponseEntity.ok(Map.of("message", "Newsletter sent successfully"));
        } catch (Exception e) {
            log.error("MANUAL_NEWSLETTER_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send newsletter"));
        }
    }

    /**
     * Health check endpoint
     * GET /subscriptions/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Newsletter Subscription Service",
                "activeSubscriptions", subscriptionService.getActiveSubscriptionCount()
        ));
    }
}

