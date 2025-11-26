package com.atena.events.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atena.events.model.User;
import com.atena.events.model.dto.LoginDTO;
import com.atena.events.model.dto.RegisterDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO register(RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já está em uso.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        userRepository.save(user);

        return new UserDTO(user);
    }

    public UserDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Senha incorreta.");
        }

        return new UserDTO(user);
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        return new UserDTO(user);
    }

    public UserDTO updateUserById(Long userId, RegisterDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }

        userRepository.save(user);

        return new UserDTO(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
