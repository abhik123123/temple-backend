
package com.temple.controller;

import com.temple.dto.EventRegistrationRequest;
import com.temple.dto.EventRegistrationResponse;
import com.temple.entity.Event;
import com.temple.security.AdminOnly;
import com.temple.service.EventRegistrationService;
import com.temple.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRegistrationService registrationService;

    @GetMapping
    public ResponseEntity<List<Event>> getAll(){
        logger.info("EVENT_FETCH_ALL");
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable Long id){
        logger.info("EVENT_FETCH_REQUEST | id={}", id);
        Event event = eventService.getEventById(id);
        if (event != null) {
            return ResponseEntity.ok(event);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create event with JSON body (Admin only)
     */
    @AdminOnly
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Event> createFromJson(
            @RequestBody Event event,
            Authentication authentication){
        logger.info("EVENT_CREATE_REQUEST_JSON | eventName={}", event.getEventName());
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            Event savedEvent = eventService.saveEvent(event, null, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
        } catch (Exception e) {
            logger.error("EVENT_CREATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Create event with optional photo using multipart (Admin only)
     */
    @AdminOnly
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Event> createWithPhoto(
            @RequestPart("event") Event event,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication){
        logger.info("EVENT_CREATE_REQUEST_MULTIPART | eventName={} | hasPhoto={}",
                   event.getEventName(), photoFile != null && !photoFile.isEmpty());
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            Event savedEvent = eventService.saveEvent(event, photoFile, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
        } catch (Exception e) {
            logger.error("EVENT_CREATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Update event with JSON body (Admin only)
     */
    @AdminOnly
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Event> updateFromJson(
            @PathVariable Long id,
            @RequestBody Event event,
            Authentication authentication){
        logger.info("EVENT_UPDATE_REQUEST_JSON | id={}", id);
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            Event updatedEvent = eventService.updateEvent(id, event, null, username);
            if (updatedEvent != null) {
                return ResponseEntity.ok(updatedEvent);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("EVENT_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Update event with optional photo using multipart (Admin only)
     */
    @AdminOnly
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Event> updateWithPhoto(
            @PathVariable Long id,
            @RequestPart("event") Event event,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile,
            Authentication authentication){
        logger.info("EVENT_UPDATE_REQUEST_MULTIPART | id={} | hasPhoto={}",
                   id, photoFile != null && !photoFile.isEmpty());
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            Event updatedEvent = eventService.updateEvent(id, event, photoFile, username);
            if (updatedEvent != null) {
                return ResponseEntity.ok(updatedEvent);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("EVENT_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Register for an event (Public)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(@RequestBody EventRegistrationRequest request) {
        logger.info("EVENT_REGISTRATION_REQUEST | eventId={} | members={}",
                   request.getEventId(), request.getMembers() != null ? request.getMembers().size() : 0);
        try {
            EventRegistrationResponse response = registrationService.registerForEvent(request);
            logger.info("EVENT_REGISTRATION_SUCCESS | registrationId={}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("EVENT_REGISTRATION_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    /**
     * Get all registrations for an event (Admin only)
     */
    @AdminOnly
    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<EventRegistrationResponse>> getEventRegistrations(@PathVariable Long id) {
        logger.info("FETCH_EVENT_REGISTRATIONS | eventId={}", id);
        List<EventRegistrationResponse> registrations = registrationService.getRegistrationsByEventId(id);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Get registration statistics for an event (Admin only)
     */
    @AdminOnly
    @GetMapping("/{id}/registration-stats")
    public ResponseEntity<?> getRegistrationStats(@PathVariable Long id) {
        logger.info("FETCH_REGISTRATION_STATS | eventId={}", id);
        Long registrationCount = registrationService.getRegistrationCountForEvent(id);
        Integer totalMembers = registrationService.getTotalMembersForEvent(id);

        return ResponseEntity.ok(new RegistrationStatsResponse(registrationCount, totalMembers));
    }

    /**
     * Cancel a registration (Admin only)
     */
    @AdminOnly
    @DeleteMapping("/registrations/{registrationId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long registrationId) {
        logger.info("CANCEL_REGISTRATION_REQUEST | registrationId={}", registrationId);
        try {
            registrationService.cancelRegistration(registrationId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("CANCEL_REGISTRATION_FAILED | error={}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete event (Admin only)
     */
    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        logger.info("EVENT_DELETE_REQUEST | id={}", id);
        if (eventService.getEventById(id) != null) {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Inner classes for response objects
    private record ErrorResponse(String error, String message) {}
    private record RegistrationStatsResponse(Long totalRegistrations, Integer totalMembers) {}
}
