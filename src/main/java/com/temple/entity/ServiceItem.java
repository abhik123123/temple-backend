package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "service_items")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonAlias({"title"})
    @NotBlank
    @Size(max = 255)
    private String name;

    /** Free text so ranges like "₹500 - ₹2000" or "Free" are possible. */
    @Size(max = 255)
    private String price;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    /** Pipe-separated bullet points, e.g. "Aarti|Prasad". */
    @Size(max = 2000)
    @Column(length = 2000)
    private String details;

    /** Minutes. */
    @Min(0)
    @Max(1440)
    private Integer duration;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    @Size(max = 20)
    @Column(length = 20)
    private String icon;

    @Builder.Default
    private boolean active = true;
}
