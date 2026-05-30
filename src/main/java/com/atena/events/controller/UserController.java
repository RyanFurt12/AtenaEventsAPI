package com.atena.events.controller;

import com.atena.events.model.User;
import com.atena.events.model.dto.AvatarUploadDTO;
import com.atena.events.model.dto.ChangePasswordDTO;
import com.atena.events.model.dto.EmailChangeRequestDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.model.dto.UserUpdateDTO;
import com.atena.events.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return userService.updateUser(id, dto, currentUser.getId());
    }

    @PostMapping("/{id}/avatar")
    public UserDTO uploadAvatar(
            @PathVariable Long id,
            @Valid @RequestBody AvatarUploadDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return userService.updateAvatar(id, dto, currentUser.getId());
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.changePassword(id, dto, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // Inicia a troca de email: envia link de confirmação para o novo endereço.
    @PostMapping("/{id}/email")
    public ResponseEntity<Void> requestEmailChange(
            @PathVariable Long id,
            @Valid @RequestBody EmailChangeRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.requestEmailChange(id, dto, currentUser.getId());
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.deleteUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
