package com.temple.service;

import com.temple.dto.EventRegistrationRequest;
import com.temple.dto.EventRegistrationResponse;
import com.temple.entity.Event;
import com.temple.entity.EventRegistration;
import com.temple.entity.EventRegistrationMember;
import com.temple.exception.BadRequestException;
import com.temple.exception.NotFoundException;
import com.temple.repository.EventRegistrationRepository;
import com.temple.repository.EventRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(EventRegistrationService.class);

    public record RegistrationStats(Long eventId, long totalRegistrations, long totalMembers) {
    }

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    public EventRegistrationService(EventRegistrationRepository registrationRepository, EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }

    public EventRegistrationResponse registerForEvent(EventRegistrationRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event", request.getEventId()));
        if (event.getEventDate() != null && event.getEventDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Registration is closed for past events");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .status("CONFIRMED")
                .build();
        List<EventRegistrationMember> members = new ArrayList<>();
        for (EventRegistrationRequest.MemberDetails m : request.getMembers()) {
            members.add(EventRegistrationMember.builder()
                    .registration(registration)
                    .name(m.getName().trim())
                    .surname(blankToNull(m.getSurname()))
                    .address(blankToNull(m.getAddress()))
                    .phoneNo(blankToNull(m.getPhoneNo()))
                    .email(blankToNull(m.getEmail()))
                    .build());
        }
        registration.setMembers(members);

        EventRegistration saved = registrationRepository.save(registration);
        logger.info("EVENT_REGISTRATION | registrationId={} | eventId={} | members={}",
                saved.getId(), event.getId(), members.size());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrationsByEventId(Long eventId) {
        requireEvent(eventId);
        return registrationRepository.findByEventId(eventId).stream().map(this::mapToResponse).toList();
    }

    public void cancelRegistration(Long id) {
        EventRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration", id));
        registration.setStatus("CANCELLED");
    }

    @Transactional(readOnly = true)
    public RegistrationStats stats(Long eventId) {
        requireEvent(eventId);
        Long registrations = registrationRepository.countActiveByEventId(eventId);
        Long members = registrationRepository.totalActiveMembersByEventId(eventId);
        return new RegistrationStats(eventId, registrations == null ? 0 : registrations, members == null ? 0 : members);
    }

    private void requireEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event", eventId);
        }
    }

    private EventRegistrationResponse mapToResponse(EventRegistration registration) {
        List<EventRegistrationResponse.MemberInfo> memberInfos = registration.getMembers().stream()
                .map(member -> EventRegistrationResponse.MemberInfo.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .surname(member.getSurname())
                        .address(member.getAddress())
                        .phoneNo(member.getPhoneNo())
                        .email(member.getEmail())
                        .build())
                .toList();
        return EventRegistrationResponse.builder()
                .id(registration.getId())
                .eventId(registration.getEvent().getId())
                .eventName(registration.getEvent().getEventName())
                .registrationDate(registration.getRegistrationDate())
                .totalMembers(registration.getTotalMembers())
                .status(registration.getStatus())
                .members(memberInfos)
                .build();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
