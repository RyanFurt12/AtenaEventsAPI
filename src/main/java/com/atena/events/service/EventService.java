package com.atena.events.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    public Event createEvent(EventCreateDTO dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setOwner(owner);
        
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventCreateDTO dto) {
        Event event = getEvent(id);
        
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = getEvent(id);
        eventRepository.delete(event);
    }

    public List<EventListResponseDTO> listEventsCreatedBy(Long userId) {
        return eventRepository.findByOwnerId(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    public List<EventListResponseDTO> listEventsParticipatedBy(Long userId) {
        return eventRepository.findByParticipantsId(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    public List<EventListResponseDTO> listRecommendedEvents() {
        List<Event> allEvents = (List<Event>) eventRepository.findAll();

        Collections.shuffle(allEvents);

        return allEvents.stream()
                .limit(8)
                .map(EventListResponseDTO::new)
                .toList();
    }

    public Event participate(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (event.getParticipants().contains(user)) {
            throw new RuntimeException("Usuário já participa deste event.");
        }

        event.getParticipants().add(user);
        return eventRepository.save(event);
    }

    public Event getOutParticipate(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        event.getParticipants().remove(user);
        return eventRepository.save(event);
    }

    public List<User> listParticipantsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        return event.getParticipants();
    }

    public List<Event> listEventsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return user.getParticipatedEvents();
    }
}
