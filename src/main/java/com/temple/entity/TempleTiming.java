package com.temple.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One opening session (a weekday can have several, e.g. morning and evening). */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "temple_timings")
public class TempleTiming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** English weekday, e.g. "Monday" (any case accepted, stored capitalised). */
    @NotBlank
    @Pattern(regexp = "(?i)^(monday|tuesday|wednesday|thursday|friday|saturday|sunday)$",
            message = "must be a weekday name")
    private String dayOfWeek;

    @NotNull
    @JsonAlias({"open"})
    private LocalTime openingTime;

    @NotNull
    @JsonAlias({"close"})
    private LocalTime closingTime;

    @Size(max = 255)
    private String specialNotes;

    @JsonIgnore
    @AssertTrue(message = "closingTime must be after openingTime")
    public boolean isTimeRangeValid() {
        return openingTime == null || closingTime == null || closingTime.isAfter(openingTime);
    }
}
