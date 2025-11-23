package com.atena.events.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.Comment;
import com.atena.events.model.dto.ComentarioCreateDTO;
import com.atena.events.model.dto.ComentarioUpdateDTO;
import com.atena.events.service.CommentService;

@RestController
@RequestMapping("/comentarios")
public class CommentController {

    @Autowired
    private CommentService comentarioService;

    @GetMapping("/evento/{eventoId}")
    public List<Comment> listarPorEvento(@PathVariable Long eventoId) {
        return comentarioService.listarPorEvento(eventoId);
    }

    @PostMapping
    public Comment criarComentario(@RequestBody ComentarioCreateDTO dto) {
        return comentarioService.criarComentario(
                dto.getEventoId(),
                dto.getUsuarioId(),
                dto.getTexto()
        );
    }

    @PutMapping("/{id}")
    public Comment atualizar(
            @PathVariable Long id,
            @RequestBody ComentarioUpdateDTO dto
    ) {
        return comentarioService.atualizarComentario(id, dto.getTexto());
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        comentarioService.deletarComentario(id);
    }
}