package com.atena.events.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Comment;
import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.CommentResponseDTO;
import com.atena.events.repository.CommentRepository;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CommentResponseDTO> listCommentsByEventId(Long eventId) {
        List<Comment> comments = commentRepository.findByEventId(eventId);

        return comments.stream()
                .map(CommentResponseDTO::new)
                .toList();
    }

    public CommentResponseDTO createComment(Long eventId, Long userId, String text) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento não encontrado"
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado"
                ));

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public CommentResponseDTO updateComment(Long commentId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Comentario não encontrado"
                ));

        comment.setText(newText);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Comentario não encontrado"
                ));

        commentRepository.delete(comment);
    }
}
