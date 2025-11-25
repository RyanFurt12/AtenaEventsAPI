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
import com.atena.events.model.dto.CommentCreateDTO;
import com.atena.events.model.dto.CommentUpdateDTO;
import com.atena.events.service.CommentService;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/event/{eventId}")
    public List<Comment> listCommentsByEventId(@PathVariable Long eventId) {
        return commentService.listCommentsByEventId(eventId);
    }

    @PostMapping
    public Comment createComment(@RequestBody CommentCreateDTO dto) {
        return commentService.createComment(
                dto.getEventId(),
                dto.getUserId(),
                dto.getText()
        );
    }

    @PutMapping("/{id}")
    public Comment updateComment(
            @PathVariable Long id,
            @RequestBody CommentUpdateDTO dto
    ) {
        return commentService.updateComment(id, dto.getText());
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }
}