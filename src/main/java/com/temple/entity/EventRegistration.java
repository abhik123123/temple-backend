package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "event_registrations")
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventRegistrationMember> members = new ArrayList<>();

    @Column(name = "total_members")
    private Integer totalMembers;

    @Column(name = "status")
    private String status; // CONFIRMED, CANCELLED, PENDING

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        if (status == null) {
            status = "CONFIRMED";
        }
        if (members != null) {
            totalMembers = members.size();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (members != null) {
            totalMembers = members.size();
        }
    }
}

