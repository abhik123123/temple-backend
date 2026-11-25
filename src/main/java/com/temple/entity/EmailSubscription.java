package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "email_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class EmailSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    /** Secret for one-click unsubscribe links in emails. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false, unique = true, length = 64)
    private String unsubscribeToken;

    private LocalDateTime lastSentAt;

    @Column(columnDefinition = "VARCHAR(50)")
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String preferences;  // JSON: {frequency: "weekly", interests: ["events", "news"]}

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime subscribedAt;

    private LocalDateTime unsubscribedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (unsubscribeToken == null) {
            byte[] bytes = new byte[24];
            new java.security.SecureRandom().nextBytes(bytes);
            unsubscribeToken = java.util.HexFormat.of().formatHex(bytes);
        }
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

