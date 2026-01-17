package com.temple.service;

import com.temple.dto.EventRegistrationRequest;
import com.temple.dto.EventRegistrationResponse;
import com.temple.entity.Event;
import com.temple.entity.EventRegistration;
import com.temple.entity.EventRegistrationMember;
import com.temple.repository.EventRegistrationRepository;
import com.temple.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(EventRegistrationService.class);

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public EventRegistrationResponse registerForEvent(EventRegistrationRequest request) {
        logger.info("REGISTRATION_REQUEST | eventId={} | members={}",
                   request.getEventId(), request.getMembers().size());

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getEventId()));

        // Validate at least one member
        if (request.getMembers() == null || request.getMembers().isEmpty()) {
            throw new RuntimeException("At least one member is required for registration");
        }

        // Create registration
        EventRegistration registration = EventRegistration.builder()
            .event(event)
            .status("CONFIRMED")
            .build();

        // Add members
        List<EventRegistrationMember> members = request.getMembers().stream()
            .map(memberDto -> EventRegistrationMember.builder()
                .registration(registration)
                .name(memberDto.getName())
                .surname(memberDto.getSurname())
                .address(memberDto.getAddress())
                .phoneNo(memberDto.getPhoneNo())
                .email(memberDto.getEmail())
                .build())
            .collect(Collectors.toList());

        registration.setMembers(members);

        // Save registration
        EventRegistration savedRegistration = registrationRepository.save(registration);

        logger.info("REGISTRATION_SUCCESS | registrationId={} | eventId={} | totalMembers={}",
                   savedRegistration.getId(), event.getId(), members.size());

        return mapToResponse(savedRegistration);
    }

    public List<EventRegistrationResponse> getRegistrationsByEventId(Long eventId) {
        logger.info("FETCH_REGISTRATIONS | eventId={}", eventId);
        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);
        return registrations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public EventRegistrationResponse getRegistrationById(Long id) {
        logger.info("FETCH_REGISTRATION | id={}", id);
        EventRegistration registration = registrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        return mapToResponse(registration);
    }

    @Transactional
    public void cancelRegistration(Long id) {
        logger.info("CANCEL_REGISTRATION | id={}", id);
        EventRegistration registration = registrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
    }

    public Long getRegistrationCountForEvent(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    public Integer getTotalMembersForEvent(Long eventId) {
        return registrationRepository.getTotalMembersByEventId(eventId);
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
            .collect(Collectors.toList());

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
}

