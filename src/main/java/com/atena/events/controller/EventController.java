package com.atena.events.controller;

import java.util.List;

import com.atena.events.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.model.dto.ParticipantSummaryDTO;
import com.atena.events.service.EventService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody EventCreateDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventService.createEvent(dto, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventCreateDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        eventService.deleteEvent(id, currentUser.getId());
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

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<List<ParticipantSummaryDTO>> listParticipants(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventService.listParticipants(eventId, currentUser.getId()));
    }
}
