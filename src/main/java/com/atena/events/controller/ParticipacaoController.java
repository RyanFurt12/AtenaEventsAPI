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
import com.atena.events.model.dto.ParticipacaoDTO;
import com.atena.events.service.EventService;

@RestController
@RequestMapping("/participacoes")
public class ParticipacaoController {

    @Autowired
    private EventService eventService;

    @PostMapping("/entrar")
    public Event participar(@RequestBody ParticipacaoDTO dto) {
        return eventService.participar(dto.getEventId(), dto.getUserId());
    }

    @PostMapping("/sair")
    public Event sair(@RequestBody ParticipacaoDTO dto) {
        return eventService.sair(dto.getEventId(), dto.getUserId());
    }

    @GetMapping("/evento/{id}")
    public List<User> participantes(@PathVariable Long id) {
        return eventService.listarParticipantes(id);
    }

    @GetMapping("/usuario/{id}")
    public List<Event> eventosDoUsuario(@PathVariable Long id) {
        return eventService.listarEventosDoUsuario(id);
    }
}
