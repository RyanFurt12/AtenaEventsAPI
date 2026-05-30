package com.atena.events.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atena.events.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOwnerId(Long ownerId);
    List<Event> findByParticipations_User_Id(Long userId);
}
