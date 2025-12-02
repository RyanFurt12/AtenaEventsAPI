package com.atena.events.service;

import com.atena.events.model.Event;
import com.atena.events.model.Participation;
import com.atena.events.model.User;
import com.atena.events.model.dto.ParticipateDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;
import com.atena.events.repository.ParticipationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ParticipationService {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    public ParticipateDTO create(ParticipateDTO participation) {
        User user = userRepository.findById(participation.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuario não encontrado"
                ));

        Event event = eventRepository.findById(participation.getEventId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado"
                ));

        Participation p = new Participation();
        p.setUser(user);
        p.setEvent(event);
        p.setStatus("OK");

        participationRepository.save(p);

        return participation;
    }

    public ParticipateDTO findById(Long id) {
        Participation p = participationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Participação não encontrado"
                ));
        
        return new ParticipateDTO(p);
    }

    public ParticipateDTO update(Long id, ParticipateDTO updated) {
        Participation current = participationRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Participação não encontrado"
                        ));

        current.setStatus("OK");
        Participation p = participationRepository.save(current);

        return new ParticipateDTO(p);
    }

    public void delete(Long id) {
        participationRepository.deleteById(id);
    }

    public boolean isParticipating(Long eventId, Long userId) {
        return participationRepository.findByUserIdAndEventId(userId, eventId).isPresent();
    }

    public boolean toggle(Long eventId, Long userId) {
        var existing = participationRepository.findByUserIdAndEventId(userId, eventId);

        if (existing.isPresent()) {
            participationRepository.delete(existing.get());
            return false;
        }

        create(new ParticipateDTO(eventId, userId));
        return true;
    }

    public List<ParticipateDTO> listByEvent(Long eventId) {
        return participationRepository.findByEventId(eventId)
                .stream()
                .map(ParticipateDTO::new)
                .toList();
    }

    public List<ParticipateDTO> listByUser(Long userId) {
        return participationRepository.findByUserId(userId)
                .stream()
                .map(ParticipateDTO::new)
                .toList();
    }
}
