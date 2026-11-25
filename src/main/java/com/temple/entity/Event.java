package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Exposed to the UI as "title"; "eventName" is still accepted for older clients. */
    @JsonProperty("title")
    @JsonAlias({"eventName"})
    @NotBlank
    @Size(max = 255)
    @Column(name = "event_name")
    private String eventName;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @JsonProperty("eventDate")
    @JsonAlias({"date"})
    @NotNull
    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(max = 255)
    private String location;

    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String status;

    /** Set by the server when a photo is uploaded; clients cannot change it. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @JsonIgnore
    @AssertTrue(message = "endTime must be after startTime")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
