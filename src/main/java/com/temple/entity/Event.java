
package com.temple.entity;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("eventName")
    @JsonAlias({"title", "eventName"})
    private String eventName;

    @Column(length=2000)
    private String description;

    @JsonProperty("eventDate")
    @JsonAlias({"date", "eventDate"})
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String category;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}
