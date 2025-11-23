package com.atena.events.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atena.events.model.User;
import com.atena.events.model.dto.LoginDTO;
import com.atena.events.model.dto.RegisterDTO;
import com.atena.events.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registrar(RegisterDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já está em uso.");
        }

        User user = new User();
        user.setNome(dto.getNome());
        user.setEmail(dto.getEmail());
        user.setSenha(dto.getSenha());

        return userRepository.save(user);
    }

    public User login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!user.getSenha().equals(dto.getSenha())) {
            throw new RuntimeException("Senha incorreta.");
        }

        return user;
    }

    public User buscar(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    public List<User> listar() {
        return (List<User>) userRepository.findAll();
    }

    public User atualizar(Long id, RegisterDTO dto) {
        User user = buscar(id);

        user.setNome(dto.getNome());
        user.setEmail(dto.getEmail());
        if (dto.getSenha() != null) {
            user.setSenha(dto.getSenha());
        }

        return userRepository.save(user);
    }

    public void deletar(Long id) {
        userRepository.deleteById(id);
    }
}
