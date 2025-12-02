package com.atena.events.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.atena.events.model.Participation;

public interface ParticipationRepository extends CrudRepository<Participation, Long> {
    Optional<Participation> findByUserIdAndEventId(Long userId, Long eventId);
    List<Participation> findByEventId(Long eventId);
    List<Participation> findByUserId(Long userId);
}
