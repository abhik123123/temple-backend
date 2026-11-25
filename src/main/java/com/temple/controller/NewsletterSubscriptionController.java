package com.temple.controller;

import com.temple.entity.EmailSubscription;
import com.temple.entity.SubscriptionLog;
import com.temple.exception.BadRequestException;
import com.temple.service.NewsletterSubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/subscriptions. Subscribe/unsubscribe are public; everything under
 * /admin and the preference/log endpoints require ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/subscriptions")
public class NewsletterSubscriptionController {

    public record SubscribeRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @Size(max = 255) String fullName,
            @Size(max = 50) String phone) {
    }

    public record EmailRequest(@NotBlank @Email @Size(max = 254) String email) {
    }

    public record PreferencesRequest(@NotBlank @Size(max = 2000) String preferences) {
    }

    public record Result(boolean success, String message, String email) {
    }

    private final NewsletterSubscriptionService subscriptionService;

    public NewsletterSubscriptionController(NewsletterSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/subscribe")
    public Result subscribe(@Valid @RequestBody SubscribeRequest request) {
        EmailSubscription sub = subscriptionService.subscribe(request.email(), request.fullName(), request.phone());
        return new Result(true, "Subscribed to temple news", sub.getEmail());
    }

    /** Accepts {"email": "..."} (preferred) or ?email= (older clients). Always succeeds, so it can't reveal who is subscribed. */
    @PostMapping("/unsubscribe")
    public Result unsubscribe(@RequestBody(required = false) EmailRequest body,
                              @RequestParam(required = false) @Email @Size(max = 254) String email) {
        String address = body != null && body.email() != null ? body.email() : email;
        if (address == null || address.isBlank()) {
            throw new BadRequestException("email is required");
        }
        subscriptionService.unsubscribe(address);
        return new Result(true, "If that address was subscribed, it has been removed", null);
    }

    /** One-click link from the footer of every newsletter email. */
    @GetMapping(path = "/unsubscribe", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> unsubscribeLink(@RequestParam @Size(max = 64) String token) {
        subscriptionService.unsubscribeByToken(token);
        return ResponseEntity.ok("You have been unsubscribed from Raja Rajeshwara Temple news.");
    }

    @GetMapping("/admin/all")
    public List<NewsletterSubscriptionService.SubscriberView> all(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return subscriptionService.list(page, size);
    }

    @GetMapping("/admin/stats")
    public Map<String, Object> stats() {
        return subscriptionService.getSubscriptionStats();
    }

    @GetMapping("/admin/logs/{subscriptionId}")
    public List<SubscriptionLog> logs(@PathVariable Long subscriptionId) {
        return subscriptionService.getSubscriptionLogs(subscriptionId);
    }

    @PostMapping("/admin/send-test")
    public Result sendTest(@Valid @RequestBody(required = false) EmailRequest body,
                           @RequestParam(required = false) @Email String email) {
        String address = body != null && body.email() != null ? body.email() : email;
        if (address == null || address.isBlank()) {
            throw new BadRequestException("email is required");
        }
        subscriptionService.sendTestEmail(address);
        return new Result(true, "Test email sent", address);
    }

    @PostMapping("/admin/send-newsletter")
    public Map<String, Integer> sendNewsletterNow() {
        return subscriptionService.sendWeeklyNewsletter();
    }

    @PutMapping("/{subscriptionId}/preferences")
    public EmailSubscription updatePreferences(@PathVariable Long subscriptionId,
                                               @Valid @RequestBody PreferencesRequest request) {
        return subscriptionService.updatePreferences(subscriptionId, request.preferences());
    }
}
