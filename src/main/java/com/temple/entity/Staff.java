package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonAlias({"name"})
    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String position;

    @Size(max = 255)
    private String department;

    @JsonAlias({"phone"})
    @Size(max = 30)
    @Pattern(regexp = "^[0-9+()\\-.\\s]*$", message = "must be a valid phone number")
    private String phoneNumber;

    @Email
    @Size(max = 255)
    private String email;

    @JsonAlias({"bio", "description"})
    @Size(max = 3000)
    @Column(length = 3000)
    private String biography;

    /** Years of experience / service. */
    @Min(0)
    @Max(100)
    private Integer experience;

    @Size(max = 500)
    @Pattern(regexp = "^(|/.*|https://.*)$", message = "must be an https:// URL or a site-relative path")
    @Column(length = 500)
    private String profileImageUrl;

    @Size(max = 255)
    private String status;
}
