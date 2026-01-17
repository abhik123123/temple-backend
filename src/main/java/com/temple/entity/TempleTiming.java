
package com.temple.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="temple_timings")
public class TempleTiming {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String specialNotes;
}
