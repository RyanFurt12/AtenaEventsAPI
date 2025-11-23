package com.atena.events.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.atena.events.model.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
    List<Event> findByOwnerId(Long ownerId);
    List<Event> findByParticipantesId(Long userId);
}
