package com.temple.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    private Long eventId;
    private String eventName;
    private String eventDate;
    private List<MemberDetails> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberDetails {
        private String name;
        private String surname;
        private String address;
        private String phoneNo;
        private String email;
    }
}

