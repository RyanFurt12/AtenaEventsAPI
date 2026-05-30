package com.atena.events.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Comment;
import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.model.dto.CommentResponseDTO;
import com.atena.events.repository.CommentRepository;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> listCommentsByEventId(Long eventId) {
        return commentRepository.findByEventId(eventId).stream()
                .map(CommentResponseDTO::new)
                .toList();
    }

    public CommentResponseDTO createComment(Long eventId, Long currentUserId, String text) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public CommentResponseDTO updateComment(Long commentId, String newText, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode editar este comentário");
        }

        if (comment.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O prazo de 24 horas para editar este comentário expirou");
        }

        comment.setText(newText);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return new CommentResponseDTO(comment);
    }

    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));

        boolean isAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isEventOwner = comment.getEvent().getOwner().getId().equals(currentUserId);

        if (!isAuthor && !isEventOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para remover este comentário");
        }

        if (isAuthor && !isEventOwner && comment.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O prazo de 24 horas para remover este comentário expirou");
        }

        commentRepository.delete(comment);
    }
}
