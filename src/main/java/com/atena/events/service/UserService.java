package com.atena.events.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.User;
import com.atena.events.model.dto.LoginDTO;
import com.atena.events.model.dto.RegisterDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User getEntityUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado."
                ));
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;

        String passwordRegex =
            "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$";

        return password.matches(passwordRegex);
    }

    public UserDTO register(RegisterDTO dto) {

        if (!isValidEmail(dto.getEmail()) || userRepository.findByEmail(dto.getEmail()).isPresent()) 
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Email inválido."
            );

        if (!isValidPassword(dto.getPassword())) throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Senha inválida. Deve conter ao menos 8 caracteres, uma letra maiúscula e um número."
        );

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        return new UserDTO(user);
    }

    public UserDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado."
                ));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Senha incorreta."
            );
        }

        return new UserDTO(user);
    }

    public UserDTO getUserById(Long userId) {
        User user = getEntityUserById(userId);

        return new UserDTO(user);
    }

    public UserDTO updateUserById(Long userId, RegisterDTO dto) {
        User user = getEntityUserById(userId);

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);

        return new UserDTO(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
