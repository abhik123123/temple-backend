package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="bhojana_shala")
public class BhojanaShala {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Integer capacity;
    private String timings;
    private String serviceType;
    private String status;
    @Builder.Default
    private boolean active = true;
}
