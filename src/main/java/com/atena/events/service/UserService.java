package com.atena.events.service;

import com.atena.events.model.EmailChangeToken;
import com.atena.events.model.User;
import com.atena.events.model.dto.AvatarUploadDTO;
import com.atena.events.model.dto.ChangePasswordDTO;
import com.atena.events.model.dto.EmailChangeRequestDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.model.dto.UserUpdateDTO;
import com.atena.events.repository.EmailChangeTokenRepository;
import com.atena.events.repository.PasswordResetTokenRepository;
import com.atena.events.repository.RefreshTokenRepository;
import com.atena.events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_AVATAR_BASE64_LENGTH = 6_291_456; // ~6MB (≈ 4.5MB image)

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final long resetTokenExpirationMinutes;

    public UserService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailChangeTokenRepository emailChangeTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${app.security.reset-token-expiration-minutes:30}") long resetTokenExpirationMinutes
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailChangeTokenRepository = emailChangeTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.resetTokenExpirationMinutes = resetTokenExpirationMinutes;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = getEntityUserById(userId);
        int eventsCount = user.getCreatedEvents() != null ? user.getCreatedEvents().size() : 0;
        int participationsCount = user.getParticipations() != null ? user.getParticipations().size() : 0;
        return new UserDTO(user, eventsCount, participationsCount);
    }

    public UserDTO updateUser(Long userId, UserUpdateDTO dto, Long authenticatedUserId) {
        verifyOwnership(userId, authenticatedUserId);

        User user = getEntityUserById(userId);
        // O email NÃO é alterado aqui — troca de email exige senha atual + verificação
        // (ver requestEmailChange / AuthController.confirmEmail).
        user.setName(dto.getName());
        userRepository.save(user);

        return new UserDTO(user);
    }

    public UserDTO updateAvatar(Long userId, AvatarUploadDTO dto, Long authenticatedUserId) {
        verifyOwnership(userId, authenticatedUserId);

        String avatarBase64 = dto.getAvatarBase64();

        if (!avatarBase64.startsWith("data:image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagem inválido.");
        }

        if (avatarBase64.length() > MAX_AVATAR_BASE64_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem muito grande. Máximo permitido: 4.5MB.");
        }

        User user = getEntityUserById(userId);
        user.setAvatarBase64(avatarBase64);
        userRepository.save(user);

        return new UserDTO(user);
    }

    /**
     * Troca a senha de um usuário autenticado, exigindo a senha atual.
     */
    public void changePassword(Long userId, ChangePasswordDTO dto, Long authenticatedUserId) {
        verifyOwnership(userId, authenticatedUserId);

        User user = getEntityUserById(userId);

        if (user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Esta conta não usa senha. Faça login pelo provedor social.");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Inicia a troca de email: valida senha atual e unicidade, cria um token e
     * envia um link de confirmação para o NOVO email. O email só é efetivamente
     * trocado quando o link é confirmado (ver AuthController.confirmEmail).
     */
    @Transactional
    public void requestEmailChange(Long userId, EmailChangeRequestDTO dto, Long authenticatedUserId) {
        verifyOwnership(userId, authenticatedUserId);

        User user = getEntityUserById(userId);

        if (user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Esta conta não usa senha. Faça login pelo provedor social.");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta.");
        }

        String newEmail = dto.getNewEmail();

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este já é o seu email atual.");
        }

        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está em uso.");
            }
        });

        // Substitui qualquer pedido de troca anterior deste usuário
        emailChangeTokenRepository.deleteByUserId(userId);

        EmailChangeToken token = new EmailChangeToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setNewEmail(newEmail);
        token.setExpiresAt(Instant.now().plus(resetTokenExpirationMinutes, ChronoUnit.MINUTES));
        emailChangeTokenRepository.save(token);

        mailService.sendEmailChangeConfirmation(newEmail, token.getToken());
    }

    @Transactional
    public void deleteUser(Long userId, Long authenticatedUserId) {
        verifyOwnership(userId, authenticatedUserId);

        User user = getEntityUserById(userId);
        refreshTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);
        emailChangeTokenRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void verifyOwnership(Long userId, Long authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode editar seu próprio perfil.");
        }
    }

    private User getEntityUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado."
                ));
    }
}
