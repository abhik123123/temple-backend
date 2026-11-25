package com.temple.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    @NotNull
    private Long eventId;

    /** Ignored (the event is looked up by id); kept for backwards compatibility. */
    private String eventName;

    /** Ignored; kept for backwards compatibility. */
    private String eventDate;

    @NotEmpty
    @Size(max = 10, message = "at most 10 members per registration")
    private List<@Valid MemberDetails> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberDetails {

        @NotBlank
        @Size(max = 255)
        private String name;

        @Size(max = 255)
        private String surname;

        @Size(max = 500)
        private String address;

        @Size(max = 30)
        @Pattern(regexp = "^[0-9+()\\-.\\s]*$", message = "must be a valid phone number")
        private String phoneNo;

        @Email
        @Size(max = 255)
        private String email;
    }
}
