package com.atena.events.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atena.events.model.User;
import com.atena.events.model.dto.LoginDTO;
import com.atena.events.model.dto.RegisterDTO;
import com.atena.events.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registrar(@RequestBody RegisterDTO dto) {
        return userService.registrar(dto);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userService.buscar(id);
    }

    @GetMapping
    public List<User> listar() {
        return userService.listar();
    }

    @PutMapping("/{id}")
    public User atualizar(@PathVariable Long id, @RequestBody RegisterDTO dto) {
        return userService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        userService.deletar(id);
        return ResponseEntity.ok().build();
    }
}
