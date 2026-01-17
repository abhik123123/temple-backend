package com.temple.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationResponse {

    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime registrationDate;
    private Integer totalMembers;
    private String status;
    private List<MemberInfo> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private String name;
        private String surname;
        private String address;
        private String phoneNo;
        private String email;
    }
}

