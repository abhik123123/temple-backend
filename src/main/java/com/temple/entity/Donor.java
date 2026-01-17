package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="donors")
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String email;

    private String name;

    private Double donationAmount;

    private String donationType;

    private String phoneNumber;

    private String address;

    private String city;

    private String state;

    private String zipCode;
}

