package com.atena.events.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.atena.events.model.Comment;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByEventoId(Long eventoId);
}
