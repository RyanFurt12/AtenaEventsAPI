package com.atena.events.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.atena.events.model.PostIt;

public interface PostItRepository extends CrudRepository<PostIt, Long> {
    // Ordem crescente de criação → o mais antigo fica em cima no render (z-index).
    List<PostIt> findByEventIdOrderByCreatedAtAsc(Long eventId);

    long countByEventIdAndAuthorId(Long eventId, Long authorId);
}
