package com.atena.events.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atena.events.model.Comment;
import com.atena.events.model.Event;
import com.atena.events.model.User;
import com.atena.events.repository.CommentRepository;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.UserRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository comentarioRepository;

    @Autowired
    private EventRepository eventoRepository;

    @Autowired
    private UserRepository usuarioRepository;

    public List<Comment> listarPorEvento(Long eventoId) {
        return comentarioRepository.findByEventoId(eventoId);
    }

    public Comment criarComentario(Long eventoId, Long usuarioId, String texto) {
        Event evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        User usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Comment comentario = new Comment();
        comentario.setEvento(evento);
        comentario.setAutor(usuario);
        comentario.setTexto(texto);
        comentario.setCriadoEm(LocalDateTime.now());

        return comentarioRepository.save(comentario);
    }

    public Comment atualizarComentario(Long comentarioId, String novoTexto) {
        Comment comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        comentario.setTexto(novoTexto);
        comentario.setAtualizadoEm(LocalDateTime.now());

        return comentarioRepository.save(comentario);
    }

    public void deletarComentario(Long comentarioId) {
        Comment comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        comentarioRepository.delete(comentario);
    }
}
