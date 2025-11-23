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
import com.atena.events.service.EventService;

@RestController
@RequestMapping("/eventos")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvento(id));
    }

    @PostMapping("/create/{ownerId}")
    public ResponseEntity<Event> criarEvento(
            @RequestBody Event evento,
            @PathVariable Long ownerId
    ) {
        return ResponseEntity.ok(eventService.criarEvento(evento, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> atualizarEvento(
            @PathVariable Long id,
            @RequestBody Event novoEvento
    ) {
        return ResponseEntity.ok(eventService.atualizarEvento(id, novoEvento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEvento(@PathVariable Long id) {
        eventService.deletarEvento(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/criados/{userId}")
    public ResponseEntity<List<Event>> listarCriados(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.listarCriados(userId));
    }

    @GetMapping("/participa/{userId}")
    public ResponseEntity<List<Event>> listarParticipa(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.listarParticipa(userId));
    }

    @GetMapping("/recomendacoes")
    public ResponseEntity<List<Event>> recomendar() {
        return ResponseEntity.ok(eventService.recomendarEventos());
    }
}
