package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="sponsorships")
@Builder
public class Sponsorship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String donorName;
    private Double amount;
    private String purpose;
    private LocalDate date;
    private String status;
    @Builder.Default
    private boolean active = true;
}
