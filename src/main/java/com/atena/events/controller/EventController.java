package com.atena.events.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.Event;
import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.service.EventService;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PostMapping("/create/{ownerId}")
    public ResponseEntity<Event> createEvent(
            @RequestBody EventCreateDTO dto,
            @PathVariable Long ownerId
    ) {
        return ResponseEntity.ok(eventService.createEvent(dto, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody EventCreateDTO dto
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/created_by/{userId}")
    public ResponseEntity<List<EventListResponseDTO>> listEventsCreatedBy(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.listEventsCreatedBy(userId));
    }

    @GetMapping("/participated_by/{userId}")
    public ResponseEntity<List<EventListResponseDTO>> listEventsParticipatedBy(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.listEventsParticipatedBy(userId));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<EventListResponseDTO>> listRecommendedEvents() {
        return ResponseEntity.ok(eventService.listRecommendedEvents());
    }
}
