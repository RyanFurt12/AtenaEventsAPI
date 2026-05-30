package com.atena.events.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.model.dto.ParticipantSummaryDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public EventDTO getEvent(Long id) {
        Event event = findEventOrThrow(id);
        return new EventDTO(event);
    }

    public EventDTO createEvent(EventCreateDTO dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado."
                ));

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImageBase64(dto.getImageBase64());
        event.setOwner(owner);
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public EventDTO updateEvent(Long id, EventCreateDTO dto, Long authenticatedUserId) {
        Event event = findEventOrThrow(id);
        verifyOwnership(event, authenticatedUserId);

        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImageBase64(dto.getImageBase64());
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public void deleteEvent(Long id, Long authenticatedUserId) {
        Event event = findEventOrThrow(id);
        verifyOwnership(event, authenticatedUserId);
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listEventsCreatedBy(Long userId) {
        return eventRepository.findByOwnerId(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listEventsParticipatedBy(Long userId) {
        return eventRepository.findByParticipations_User_Id(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listRecommendedEvents() {
        List<Event> allEvents = (List<Event>) eventRepository.findAll();
        Collections.shuffle(allEvents);
        return allEvents.stream()
                .limit(8)
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipantSummaryDTO> listParticipants(Long eventId, Long requesterId) {
        Event event = findEventOrThrow(eventId);
        verifyOwnership(event, requesterId);
        return event.getParticipations().stream()
                .map(ParticipantSummaryDTO::new)
                .toList();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado."
                ));
    }

    private void verifyOwnership(Event event, Long authenticatedUserId) {
        if (!event.getOwner().getId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Você não tem permissão para modificar este evento."
            );
        }
    }
}
