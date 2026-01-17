package com.temple.service;

import com.temple.entity.Event;
import com.temple.entity.Photo;
import com.temple.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private IntegratedPhotoService integratedPhotoService;

    /**
     * Get all events, automatically excluding past events
     */
    public List<Event> getAllEvents() {
        LocalDate today = LocalDate.now();
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .filter(event -> event.getEventDate() != null && !event.getEventDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    /**
     * Save event with optional photo upload
     */
    public Event saveEvent(Event event, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("EVENT_CREATE_WITH_PHOTO | eventName={} | hasPhoto={}",
                   event.getEventName(), photoFile != null && !photoFile.isEmpty());

        // Save event first
        Event savedEvent = eventRepository.save(event);
        logger.debug("EVENT_SAVED | eventId={}", savedEvent.getId());

        // Save photo if provided
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                Photo photo = integratedPhotoService.savePhotoForEntity(
                        "events",
                        savedEvent.getId(),
                        photoFile,
                        uploadedBy,
                        "Event photo - " + event.getEventName()
                );

                if (photo != null) {
                    savedEvent.setImageUrl(photo.getPhotoUrl());
                    eventRepository.save(savedEvent);
                    logger.info("EVENT_PHOTO_SAVED | eventId={} | photoId={}", savedEvent.getId(), photo.getId());
                }
            } catch (IOException e) {
                logger.error("EVENT_PHOTO_SAVE_FAILED | error={}", e.getMessage());
                // Continue without photo - don't fail the event creation
            }
        }

        return savedEvent;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public Event saveEvent(Event event) {
        try {
            return saveEvent(event, null, "system");
        } catch (IOException e) {
            logger.error("EVENT_SAVE_ERROR | error={}", e.getMessage());
            return eventRepository.save(event);
        }
    }

    /**
     * Update event with optional photo upload
     */
    public Event updateEvent(Long id, Event event, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("EVENT_UPDATE_WITH_PHOTO | eventId={} | hasPhoto={}",
                   id, photoFile != null && !photoFile.isEmpty());

        if (eventRepository.existsById(id)) {
            event.setId(id);
            Event updatedEvent = eventRepository.save(event);
            logger.debug("EVENT_UPDATED | eventId={}", id);

            // Update photo if provided
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    // Delete old photo
                    integratedPhotoService.deletePhotoForEntity(id);

                    // Save new photo
                    Photo photo = integratedPhotoService.savePhotoForEntity(
                            "events",
                            id,
                            photoFile,
                            uploadedBy,
                            "Event photo - " + event.getEventName()
                    );

                    if (photo != null) {
                        updatedEvent.setImageUrl(photo.getPhotoUrl());
                        eventRepository.save(updatedEvent);
                        logger.info("EVENT_PHOTO_UPDATED | eventId={} | photoId={}", id, photo.getId());
                    }
                } catch (IOException e) {
                    logger.error("EVENT_PHOTO_UPDATE_FAILED | error={}", e.getMessage());
                    // Continue without photo update
                }
            }

            return updatedEvent;
        }
        return null;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public Event updateEvent(Long id, Event event) {
        try {
            return updateEvent(id, event, null, "system");
        } catch (IOException e) {
            logger.error("EVENT_UPDATE_ERROR | error={}", e.getMessage());
            if (eventRepository.existsById(id)) {
                event.setId(id);
                return eventRepository.save(event);
            }
            return null;
        }
    }

    /**
     * Delete event (also deletes associated photos)
     */
    public void deleteEvent(Long id) {
        logger.info("EVENT_DELETE | eventId={}", id);
        // Delete photos
        integratedPhotoService.deletePhotoForEntity(id);
        // Delete event
        eventRepository.deleteById(id);
    }

    /**
     * Scheduled task to automatically delete past events
     * Runs every day at 00:00 (midnight)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredEvents() {
        try {
            LocalDate today = LocalDate.now();
            List<Event> pastEvents = eventRepository.findPastEvents(today);

            if (!pastEvents.isEmpty()) {
                int deletedCount = eventRepository.deletePastEvents(today);
                log.info("✅ Auto-deleted {} past event(s)", deletedCount);
            }
        } catch (Exception e) {
            log.error("❌ Error while deleting past events: ", e);
        }
    }
}

