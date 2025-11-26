package com.atena.events.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .map(c -> new CommentResponseDTO(
                        c.getText(),
                        c.getAuthor().getName(),
                        c.getCreatedAt()))
                .toList();
    }

    public Comment createComment(Long eventId, Long userId, String text) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        comment.setText(newText);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        commentRepository.delete(comment);
    }
}
