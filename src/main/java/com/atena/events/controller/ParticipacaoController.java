package com.atena.events.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.ParticipateDTO;
import com.atena.events.service.EventService;

@RestController
@RequestMapping("/participate")
public class ParticipacaoController {

    @Autowired
    private EventService eventService;

    @PostMapping("/in")
    public Event participate(@RequestBody ParticipateDTO dto) {
        return eventService.participate(dto.getEventId(), dto.getUserId());
    }

    @PostMapping("/out")
    public Event getOutParticipate(@RequestBody ParticipateDTO dto) {
        return eventService.getOutParticipate(dto.getEventId(), dto.getUserId());
    }

    @GetMapping("/event/{id}")
    public List<User> listParticipantsByEventId(@PathVariable Long eventId) {
        return eventService.listParticipantsByEventId(eventId);
    }

    @GetMapping("/user/{id}")
    public List<Event> listEventsByUserId(@PathVariable Long userId) {
        return eventService.listEventsByUserId(userId);
    }
}
