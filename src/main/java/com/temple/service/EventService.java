package com.temple.service;

import com.temple.entity.Event;
import com.temple.entity.Photo;
import com.temple.exception.NotFoundException;
import com.temple.repository.EventRegistrationRepository;
import com.temple.repository.EventRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Events are kept after they happen (history, registrations, reports); the
 * public site asks for {@code ?upcoming=true}. The old nightly job that
 * deleted past events was removed: it destroyed history and failed as soon as
 * an event had registrations.
 */
@Service
@Transactional
public class EventService {

    static final String PHOTO_TYPE = "events";

    private static final Comparator<Event> BY_DATE_AND_TIME = Comparator
            .comparing(Event::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Event::getStartTime, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final PhotoService photoService;

    public EventService(EventRepository eventRepository, EventRegistrationRepository registrationRepository,
                        PhotoService photoService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.photoService = photoService;
    }

    @Transactional(readOnly = true)
    public List<Event> getEvents(boolean upcomingOnly) {
        LocalDate today = LocalDate.now();
        return eventRepository.findAll().stream()
                .filter(e -> !upcomingOnly || (e.getEventDate() != null && !e.getEventDate().isBefore(today)))
                .sorted(BY_DATE_AND_TIME)
                .toList();
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event", id));
    }

    public Event create(Event event, MultipartFile photoFile, String uploadedBy) {
        event.setId(null);
        event.setImageUrl(null);
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("Upcoming");
        }
        Event saved = eventRepository.save(event);
        attachPhoto(saved, photoFile, uploadedBy);
        return saved;
    }

    /** Copies editable fields onto the stored event, so an update without a photo keeps the existing one. */
    public Event update(Long id, Event changes, MultipartFile photoFile, String uploadedBy) {
        Event event = getEvent(id);
        event.setEventName(changes.getEventName());
        event.setDescription(changes.getDescription());
        event.setEventDate(changes.getEventDate());
        event.setStartTime(changes.getStartTime());
        event.setEndTime(changes.getEndTime());
        event.setLocation(changes.getLocation());
        event.setCategory(changes.getCategory());
        if (changes.getStatus() != null && !changes.getStatus().isBlank()) {
            event.setStatus(changes.getStatus());
        }
        attachPhoto(event, photoFile, uploadedBy);
        return event;
    }

    public void delete(Long id) {
        Event event = getEvent(id);
        registrationRepository.deleteAll(registrationRepository.findByEventId(id));
        photoService.deleteForEntity(PHOTO_TYPE, id);
        eventRepository.delete(event);
    }

    private void attachPhoto(Event event, MultipartFile photoFile, String uploadedBy) {
        if (photoFile == null || photoFile.isEmpty()) {
            return;
        }
        Photo photo = photoService.replaceForEntity(PHOTO_TYPE, event.getId(), photoFile, uploadedBy,
                "Event photo - " + event.getEventName());
        event.setImageUrl(photo.getPhotoUrl());
    }
}
