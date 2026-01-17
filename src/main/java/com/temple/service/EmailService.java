package com.temple.service;

import com.temple.entity.EmailSubscription;
import com.temple.entity.Event;
import com.temple.entity.SubscriptionLog;
import com.temple.repository.SubscriptionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private SubscriptionLogRepository subscriptionLogRepository;

    @Autowired
    private EventService eventService;

    @Value("${spring.mail.username:noreply@rajarajeshwara.com}")
    private String fromEmail;

    /**
     * Send HTML email with temple events for the week
     */
    public boolean sendWeeklyNewsletter(EmailSubscription subscription) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping email for: {}", subscription.getEmail());
                return false;
            }

            List<Event> upcomingEvents = eventService.getAllEvents();
            String htmlContent = buildNewsletterHtml(subscription, upcomingEvents);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(subscription.getEmail());
            helper.setSubject("🏛️ Raja Rajeshwara Temple - Weekly Events Newsletter");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // Log success
            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscription(subscription)
                    .actionType("EMAIL_SENT")
                    .emailSubject("Weekly Events Newsletter")
                    .details("Weekly newsletter sent successfully with " + upcomingEvents.size() + " events")
                    .build();
            subscriptionLogRepository.save(subscriptionLog);

            log.info("NEWSLETTER_SENT | email={} | eventCount={}", subscription.getEmail(), upcomingEvents.size());
            return true;

        } catch (MessagingException e) {
            log.error("NEWSLETTER_SEND_FAILED | email={} | error={}", subscription.getEmail(), e.getMessage());

            // Log failure
            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscription(subscription)
                    .actionType("EMAIL_FAILED")
                    .emailSubject("Weekly Events Newsletter")
                    .errorMessage(e.getMessage())
                    .details("Failed to send weekly newsletter")
                    .build();
            subscriptionLogRepository.save(subscriptionLog);

            return false;
        }
    }

    /**
     * Build HTML content for newsletter
     */
    private String buildNewsletterHtml(EmailSubscription subscription, List<Event> events) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; color: #333; background-color: #f5f5f5; }\n");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }\n");
        html.append(".header { background-color: #0B1C3F; color: #E6B325; padding: 20px; text-align: center; border-radius: 8px; }\n");
        html.append(".header h1 { margin: 0; font-size: 28px; }\n");
        html.append(".event { border-left: 4px solid #E6B325; padding: 15px; margin: 15px 0; background-color: #f9f9f9; }\n");
        html.append(".event-title { font-size: 18px; font-weight: bold; color: #0B1C3F; }\n");
        html.append(".event-date { color: #E6B325; font-weight: bold; margin: 5px 0; }\n");
        html.append(".event-details { color: #666; font-size: 14px; margin: 5px 0; }\n");
        html.append(".footer { border-top: 1px solid #ddd; margin-top: 30px; padding-top: 20px; text-align: center; font-size: 12px; color: #999; }\n");
        html.append(".unsubscribe { color: #0B1C3F; text-decoration: none; }\n");
        html.append(".no-events { color: #666; font-style: italic; padding: 20px; text-align: center; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"container\">\n");

        // Header
        html.append("<div class=\"header\">\n");
        html.append("<h1>🏛️ Raja Rajeshwara Temple</h1>\n");
        html.append("<p style=\"margin: 10px 0; font-size: 16px;\">Weekly Events & News</p>\n");
        html.append("</div>\n");

        // Greeting
        html.append("<p>Dear ").append(subscription.getFullName()).append(",</p>\n");
        html.append("<p>Namaste! Here are the upcoming events at Raja Rajeshwara Temple this week:</p>\n");

        // Events
        if (events != null && !events.isEmpty()) {
            for (Event event : events) {
                html.append("<div class=\"event\">\n");
                html.append("<div class=\"event-title\">").append(event.getEventName()).append("</div>\n");
                if (event.getEventDate() != null) {
                    html.append("<div class=\"event-date\">📅 ")
                            .append(event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                            .append(" at ")
                            .append(event.getStartTime() != null ? event.getStartTime().toString() : "TBD")
                            .append("</div>\n");
                }
                if (event.getLocation() != null) {
                    html.append("<div class=\"event-details\">📍 Location: ").append(event.getLocation()).append("</div>\n");
                }
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    html.append("<div class=\"event-details\">").append(event.getDescription()).append("</div>\n");
                }
                html.append("</div>\n");
            }
        } else {
            html.append("<div class=\"no-events\">No events scheduled this week. Please check back soon!</div>\n");
        }

        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("<p>© 2025 Raja Rajeshwara Devastanam. All rights reserved.</p>\n");
        html.append("<p>📍 Near Old Market, Hasaparthy, Warangal | 📞 (555) 123-4567 | 📧 info@rajarajeshwara.com</p>\n");
        html.append("<p><a class=\"unsubscribe\" href=\"http://localhost:8080/subscriptions/unsubscribe?email=")
                .append(subscription.getEmail())
                .append("\">Unsubscribe from newsletter</a></p>\n");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * Send confirmation email for new subscription
     */
    public boolean sendSubscriptionConfirmation(EmailSubscription subscription) {
        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Skipping confirmation email for: {}", subscription.getEmail());
                return false;
            }

            String htmlContent = buildConfirmationHtml(subscription);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(subscription.getEmail());
            helper.setSubject("✅ Welcome to Raja Rajeshwara Temple Newsletter");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("SUBSCRIPTION_CONFIRMATION_SENT | email={}", subscription.getEmail());
            return true;

        } catch (MessagingException e) {
            log.error("SUBSCRIPTION_CONFIRMATION_FAILED | email={} | error={}", subscription.getEmail(), e.getMessage());
            return false;
        }
    }

    /**
     * Build HTML content for subscription confirmation
     */
    private String buildConfirmationHtml(EmailSubscription subscription) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "body { font-family: Arial, sans-serif; color: #333; background-color: #f5f5f5; }\n" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }\n" +
                ".header { background-color: #0B1C3F; color: #E6B325; padding: 20px; text-align: center; border-radius: 8px; }\n" +
                ".header h1 { margin: 0; font-size: 28px; }\n" +
                ".content { padding: 20px; }\n" +
                ".footer { border-top: 1px solid #ddd; margin-top: 30px; padding-top: 20px; text-align: center; font-size: 12px; color: #999; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "<div class=\"header\">\n" +
                "<h1>🏛️ Raja Rajeshwara Temple</h1>\n" +
                "<p style=\"margin: 10px 0; font-size: 16px;\">Newsletter Subscription Confirmed</p>\n" +
                "</div>\n" +
                "<div class=\"content\">\n" +
                "<p>Namaste " + subscription.getFullName() + ",</p>\n" +
                "<p>Thank you for subscribing to the Raja Rajeshwara Temple newsletter! You will now receive weekly updates about our events and activities.</p>\n" +
                "<p><strong>What to Expect:</strong></p>\n" +
                "<ul>\n" +
                "<li>Weekly event notifications every Sunday</li>\n" +
                "<li>Temple news and special announcements</li>\n" +
                "<li>Important festival dates and celebrations</li>\n" +
                "</ul>\n" +
                "<p>We look forward to keeping you connected with the temple community!</p>\n" +
                "</div>\n" +
                "<div class=\"footer\">\n" +
                "<p>© 2025 Raja Rajeshwara Devastanam. All rights reserved.</p>\n" +
                "<p>📍 Near Old Market, Hasaparthy, Warangal | 📞 (555) 123-4567</p>\n" +
                "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}

