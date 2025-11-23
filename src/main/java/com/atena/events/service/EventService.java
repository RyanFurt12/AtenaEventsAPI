package com.atena.events.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public Event getEvento(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    public Event criarEvento(Event evento, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        evento.setOwner(owner);
        return eventRepository.save(evento);
    }

    public Event atualizarEvento(Long id, Event novoEvento) {
        Event evento = getEvento(id);

        evento.setTitulo(novoEvento.getTitulo());
        evento.setDescricao(novoEvento.getDescricao());
        evento.setDate(novoEvento.getDate());

        return eventRepository.save(evento);
    }

    public void deletarEvento(Long id) {
        Event evento = getEvento(id);
        eventRepository.delete(evento);
    }

    public List<Event> listarCriados(Long userId) {
        return eventRepository.findByOwnerId(userId);
    }

    public List<Event> listarParticipa(Long userId) {
        return eventRepository.findByParticipantesId(userId);
    }

    public List<Event> recomendarEventos() {
        List<Event> todos = (List<Event>) eventRepository.findAll();

        Collections.shuffle(todos);

        return todos.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    //Participantes
    public Event participar(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (event.getParticipantes().contains(user)) {
            throw new RuntimeException("Usuário já participa deste evento.");
        }

        event.getParticipantes().add(user);
        return eventRepository.save(event);
    }

    public Event sair(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        event.getParticipantes().remove(user);
        return eventRepository.save(event);
    }

    public List<User> listarParticipantes(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        return event.getParticipantes();
    }

    public List<Event> listarEventosDoUsuario(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return user.getEventosParticipando();
    }
}
