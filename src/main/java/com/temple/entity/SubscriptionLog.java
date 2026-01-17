package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "subscription_logs")
public class SubscriptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private EmailSubscription subscription;

    @Column(nullable = false)
    private String actionType;  // "SUBSCRIBE", "UNSUBSCRIBE", "EMAIL_SENT", "EMAIL_FAILED"

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String emailSubject;  // For EMAIL_SENT/EMAIL_FAILED

    private String errorMessage;  // For EMAIL_FAILED

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

