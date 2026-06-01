package com.atena.events.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.EventCreateDTO;
import com.atena.events.model.dto.EventDTO;
import com.atena.events.model.dto.EventListResponseDTO;
import com.atena.events.model.dto.ParticipantSummaryDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        MailService mailService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public EventDTO getEvent(Long id) {
        Event event = findEventOrThrow(id);
        return new EventDTO(event);
    }

    public EventDTO createEvent(EventCreateDTO dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado."
                ));

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImageBase64(dto.getImageBase64());
        event.setOwner(owner);
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public EventDTO updateEvent(Long id, EventCreateDTO dto, Long authenticatedUserId) {
        Event event = findEventOrThrow(id);
        verifyOwnership(event, authenticatedUserId);

        event.setTitle(dto.getTitle());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setImageBase64(dto.getImageBase64());
        eventRepository.save(event);

        return new EventDTO(event);
    }

    public void deleteEvent(Long id, Long authenticatedUserId) {
        Event event = findEventOrThrow(id);
        verifyOwnership(event, authenticatedUserId);
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listEventsCreatedBy(Long userId) {
        return eventRepository.findByOwnerId(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listEventsParticipatedBy(Long userId) {
        return eventRepository.findByParticipations_User_Id(userId)
                .stream()
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDTO> listRecommendedEvents() {
        List<Event> allEvents = (List<Event>) eventRepository.findAll();
        Collections.shuffle(allEvents);
        return allEvents.stream()
                .limit(8)
                .map(EventListResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipantSummaryDTO> listParticipants(Long eventId, Long requesterId) {
        Event event = findEventOrThrow(eventId);
        verifyOwnership(event, requesterId);
        return event.getParticipations().stream()
                .map(ParticipantSummaryDTO::new)
                .toList();
    }

    /**
     * Envia uma notificação por email aos participantes do evento (apenas o dono).
     * {@code phase} = "PRE" (lembrete, só antes da data do evento) | "POST"
     * (agradecimento, só após a data). Cada fase pode ser enviada uma única vez.
     * Participantes sem email (ex.: convidados) são ignorados.
     *
     * @return número de participantes que receberam o email
     */
    public int notifyParticipants(Long eventId, Long requesterId, String phase, String customMessage) {
        Event event = findEventOrThrow(eventId);
        verifyOwnership(event, requesterId);

        boolean preEvent = "PRE".equalsIgnoreCase(phase);
        boolean postEvent = "POST".equalsIgnoreCase(phase);
        if (!preEvent && !postEvent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fase inválida. Use PRE ou POST.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (preEvent) {
            if (event.getPreEventNotifiedAt() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Lembrete pré-evento já enviado.");
            }
            if (event.getDate() != null && !event.getDate().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O lembrete pré-evento só pode ser enviado antes da data do evento.");
            }
        } else {
            if (event.getPostEventNotifiedAt() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Agradecimento pós-evento já enviado.");
            }
            if (event.getDate() != null && !event.getDate().isBefore(now)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O agradecimento pós-evento só pode ser enviado após a data do evento.");
            }
        }

        List<String> recipients = (event.getParticipations() == null ? List.<com.atena.events.model.Participation>of() : event.getParticipations())
                .stream()
                .filter(p -> p != null && "OK".equals(p.getStatus()) && p.getUser() != null)
                .map(p -> p.getUser().getEmail())
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();

        for (String email : recipients) {
            mailService.sendEventNotification(email, event.getTitle(), preEvent, customMessage);
        }

        if (preEvent) {
            event.setPreEventNotifiedAt(now);
        } else {
            event.setPostEventNotifiedAt(now);
        }
        eventRepository.save(event);

        return recipients.size();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado."
                ));
    }

    private void verifyOwnership(Event event, Long authenticatedUserId) {
        if (!event.getOwner().getId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Você não tem permissão para modificar este evento."
            );
        }
    }
}
