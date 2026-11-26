package com.temple.controller;

import com.temple.dto.EventRegistrationRequest;
import com.temple.dto.EventRegistrationResponse;
import com.temple.entity.Event;
import com.temple.service.EventRegistrationService;
import com.temple.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * /api/events. Reads and registration are public; everything else requires
 * ADMIN (enforced in SecurityConfig). Create/update accept plain JSON, or
 * multipart with an "event" JSON part and an optional "photo" part.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;

    public EventController(EventService eventService, EventRegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping
    public List<Event> list(@RequestParam(defaultValue = "false") boolean upcoming) {
        return eventService.getEvents(upcoming);
    }

    @GetMapping("/{id}")
    public Event get(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody Event event, Authentication auth) {
        return eventService.create(event, null, auth.getName());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Event createWithPhoto(@Valid @RequestPart("event") Event event,
                                 @RequestPart(value = "photo", required = false) MultipartFile photo,
                                 Authentication auth) {
        return eventService.create(event, photo, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Event update(@PathVariable Long id, @Valid @RequestBody Event event, Authentication auth) {
        return eventService.update(id, event, null, auth.getName());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Event updateWithPhoto(@PathVariable Long id,
                                 @Valid @RequestPart("event") Event event,
                                 @RequestPart(value = "photo", required = false) MultipartFile photo,
                                 Authentication auth) {
        return eventService.update(id, event, photo, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }

    /** Public: devotees register themselves and family members. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRegistrationResponse register(@Valid @RequestBody EventRegistrationRequest request) {
        return registrationService.registerForEvent(request);
    }

    /** Public, and only when the admin has switched the list on: registrants' names, no contact details. */
    @GetMapping("/{id}/attendees")
    public EventRegistrationService.PublicAttendees attendees(@PathVariable Long id) {
        return registrationService.publicAttendees(id);
    }

    public record EventSettings(Boolean registrationOpen, Boolean attendeeListPublic) {
    }

    /** Admin: flip the per-event registration switches without resending the whole event. */
    @PatchMapping(value = "/{id}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Event updateSettings(@PathVariable Long id, @RequestBody EventSettings settings) {
        return eventService.updateSettings(id, settings.registrationOpen(), settings.attendeeListPublic());
    }

    @GetMapping("/{id}/registrations")
    public List<EventRegistrationResponse> registrations(@PathVariable Long id) {
        return registrationService.getRegistrationsByEventId(id);
    }

    @GetMapping("/{id}/registration-stats")
    public EventRegistrationService.RegistrationStats registrationStats(@PathVariable Long id) {
        return registrationService.stats(id);
    }

    @DeleteMapping("/registrations/{registrationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRegistration(@PathVariable Long registrationId) {
        registrationService.cancelRegistration(registrationId);
    }
}
