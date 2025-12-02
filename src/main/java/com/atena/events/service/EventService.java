package com.atena.events.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public EventDTO getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado"
                ));

        return new EventDTO(event);
    }

    public EventDTO createEvent(EventCreateDTO dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado"
                ));

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setOwner(owner);
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public EventDTO updateEvent(Long id, EventCreateDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado"
                ));
        
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado"
                ));
        eventRepository.delete(event);
    }

    public List<EventListResponseDTO> listEventsCreatedBy(Long userId) {
        return eventRepository.findByOwnerId(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    public List<EventListResponseDTO> listEventsParticipatedBy(Long userId) {
        return eventRepository.findByParticipations_User_Id(userId)
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
}
