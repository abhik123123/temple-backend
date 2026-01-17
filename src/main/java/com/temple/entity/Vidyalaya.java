package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="vidyalaya")
public class Vidyalaya {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; // Saraswati Vidyalaya
    @Column(length=2000)
    private String description;
    private String director;
    private Integer studentsEnrolled;
    private String subjects;
    private String operatingDays;
    private String timings;
    private String status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
