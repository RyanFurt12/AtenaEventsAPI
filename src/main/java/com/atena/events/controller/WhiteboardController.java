package com.atena.events.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.User;
import com.atena.events.model.dto.PostItCreateDTO;
import com.atena.events.model.dto.PostItMoveDTO;
import com.atena.events.model.dto.PostItResponseDTO;
import com.atena.events.model.dto.WhiteboardDTO;
import com.atena.events.service.WhiteboardService;

@RestController
@RequestMapping("/events/{eventId}/whiteboard")
public class WhiteboardController {

    private final WhiteboardService whiteboardService;

    public WhiteboardController(WhiteboardService whiteboardService) {
        this.whiteboardService = whiteboardService;
    }

    @GetMapping
    public WhiteboardDTO getBoard(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        return whiteboardService.getBoard(eventId, currentUser != null ? currentUser.getId() : null);
    }

    @PostMapping("/activate")
    public WhiteboardDTO activate(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser
    ) {
        return whiteboardService.activate(eventId, currentUser.getId());
    }

    @PostMapping("/postits")
    public PostItResponseDTO addPostIt(
            @PathVariable Long eventId,
            @RequestBody PostItCreateDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return whiteboardService.addPostIt(eventId, currentUser.getId(), dto);
    }

    @PutMapping("/postits/{postItId}")
    public PostItResponseDTO movePostIt(
            @PathVariable Long eventId,
            @PathVariable Long postItId,
            @RequestBody PostItMoveDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return whiteboardService.movePostIt(postItId, currentUser.getId(), dto);
    }

    @DeleteMapping("/postits/{postItId}")
    public void deletePostIt(
            @PathVariable Long eventId,
            @PathVariable Long postItId,
            @AuthenticationPrincipal User currentUser
    ) {
        whiteboardService.deletePostIt(postItId, currentUser.getId());
    }
}
