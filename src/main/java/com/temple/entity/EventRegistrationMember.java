package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "event_registration_members")
public class EventRegistrationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    @JsonIgnore
    private EventRegistration registration;

    @Column(nullable = false)
    private String name;

    private String surname;

    @Column(length = 500)
    private String address;

    @Column(name = "phone_no")
    private String phoneNo;

    private String email;
}

