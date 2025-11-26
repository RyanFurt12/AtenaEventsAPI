package com.atena.events.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.model.dto.ParticipateDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.service.EventService;

@RestController
@RequestMapping("/participate")
public class ParticipacaoController {

    @Autowired
    private EventService eventService;

    @GetMapping("/event/{eventId}/user/{userId}")
    public Boolean isParticipating(@PathVariable Long eventId, @PathVariable Long userId) {
        return eventService.isParticipating(eventId, userId);
    }

    @PostMapping("/toggle")
    public Boolean participateToggle(@RequestBody ParticipateDTO dto) {
        return eventService.participateToggle(dto.getEventId(), dto.getUserId());
    }

    @GetMapping("/event/{eventId}")
    public List<UserDTO> listParticipantsByEventId(@PathVariable Long eventId) {
        return eventService.listParticipantsByEventId(eventId);
    }

    @GetMapping("/user/{userId}")
    public List<EventListResponseDTO> listEventsByUserId(@PathVariable Long userId) {
        return eventService.listEventsByUserId(userId);
    }
}
