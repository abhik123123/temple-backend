package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A donation record. Contact fields (email, phone, address) are only ever
 * returned to admins; the public donations page gets {@link com.temple.dto.PublicDonation}.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("donorName")
    @JsonAlias({"name"})
    @NotBlank
    @Size(max = 255)
    private String name;

    @JsonProperty("amount")
    @JsonAlias({"donationAmount"})
    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal donationAmount;

    @NotBlank
    @Size(max = 255)
    private String donationType;

    @JsonAlias({"date"})
    private LocalDate donationDate;

    @Size(max = 150)
    @Column(length = 150)
    private String inMemoryOf;

    @Size(max = 500)
    @Column(length = 500)
    private String donationReason;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 20)
    private String zipCode;
}
