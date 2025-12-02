package com.atena.events.controller;

import com.atena.events.model.dto.ParticipateDTO;
import com.atena.events.service.ParticipationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participate")
public class ParticipationController {

    @Autowired
    private ParticipationService participationService;

    @PostMapping
    public ParticipateDTO create(@RequestBody ParticipateDTO p) {
        return participationService.create(p);
    }

    @GetMapping("/{id}")
    public ParticipateDTO get(@PathVariable Long id) {
        return participationService.findById(id);
    }

    @PutMapping("/{id}")
    public ParticipateDTO update(@PathVariable Long id, @RequestBody ParticipateDTO p) {
        return participationService.update(id, p);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        participationService.delete(id);
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    public Boolean isParticipating(@PathVariable Long eventId, @PathVariable Long userId) {
        return participationService.isParticipating(eventId, userId);
    }

    @PostMapping("/toggle/event/{eventId}/user/{userId}")
    public Boolean toggle(
            @PathVariable Long eventId,
            @PathVariable Long userId
    ) {
        return participationService.toggle(eventId, userId);
    }

    @GetMapping("/event/{eventId}")
    public List<ParticipateDTO> listByEvent(@PathVariable Long eventId) {
        return participationService.listByEvent(eventId);
    }

    @GetMapping("/user/{userId}")
    public List<ParticipateDTO> listByUser(@PathVariable Long userId) {
        return participationService.listByUser(userId);
    }
}
