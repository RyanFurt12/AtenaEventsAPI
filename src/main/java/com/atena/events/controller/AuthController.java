package com.atena.events.controller;

import com.atena.events.model.EmailChangeToken;
import com.atena.events.model.PasswordResetToken;
import com.atena.events.model.RefreshToken;
import com.atena.events.model.User;
import com.atena.events.model.dto.*;
import com.atena.events.repository.EmailChangeTokenRepository;
import com.atena.events.repository.PasswordResetTokenRepository;
import com.atena.events.repository.RefreshTokenRepository;
import com.atena.events.repository.UserRepository;
import com.atena.events.security.JwtService;
import com.atena.events.service.GuestAuthService;
import com.atena.events.service.MailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GuestAuthService guestAuthService;
    private final MailService mailService;
    private final long resetTokenExpirationMinutes;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    public AuthController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailChangeTokenRepository emailChangeTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GuestAuthService guestAuthService,
            MailService mailService,
            @Value("${app.security.reset-token-expiration-minutes:30}") long resetTokenExpirationMinutes
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailChangeTokenRepository = emailChangeTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.guestAuthService = guestAuthService;
        this.mailService = mailService;
        this.resetTokenExpirationMinutes = resetTokenExpirationMinutes;
    }

    @PostMapping("/guest")
    public ResponseEntity<AuthResponseDTO> guest(@Valid @RequestBody GuestCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestAuthService.createGuest(dto));
    }

    @PostMapping("/merge-guest")
    @Transactional
    public ResponseEntity<Void> mergeGuest(
            @Valid @RequestBody MergeGuestDTO dto,
            @AuthenticationPrincipal com.atena.events.model.User principal
    ) {
        guestAuthService.mergeGuestIntoUser(principal.getId(), dto.getGuestId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upgrade/password")
    public ResponseEntity<AuthResponseDTO> upgradeWithPassword(
            @Valid @RequestBody UpgradePasswordDTO dto,
            @AuthenticationPrincipal com.atena.events.model.User principal
    ) {
        return ResponseEntity.ok(guestAuthService.upgradeWithPassword(principal.getId(), dto));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está em uso.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais inválidas."
                ));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        RefreshToken stored = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh token inválido."
                ));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado. Faça login novamente.");
        }

        // Rotaciona o refresh token (invalida o antigo, gera um novo)
        refreshTokenRepository.delete(stored);

        return ResponseEntity.ok(buildAuthResponse(stored.getUser()));
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        refreshTokenRepository.findByToken(dto.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
        return ResponseEntity.noContent().build();
    }

    // ── Password recovery ──────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        // Sempre responde 200 — não revela se o email existe ou não.
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            // Apenas contas com senha (PASSWORD) podem redefinir senha.
            if (user.getPassword() == null) return;

            // Substitui qualquer pedido anterior deste usuário
            passwordResetTokenRepository.deleteByUserId(user.getId());

            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(user);
            token.setExpiresAt(Instant.now().plus(resetTokenExpirationMinutes, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(token);

            mailService.sendPasswordReset(user.getEmail(), token.getToken());
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        PasswordResetToken stored = passwordResetTokenRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Link inválido ou já utilizado."
                ));

        if (stored.isExpired()) {
            passwordResetTokenRepository.delete(stored);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link expirado. Solicite um novo.");
        }

        User user = stored.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Invalida o token e derruba as sessões existentes
        passwordResetTokenRepository.delete(stored);
        refreshTokenRepository.deleteByUserId(user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm-email")
    @Transactional
    public ResponseEntity<Void> confirmEmail(@Valid @RequestBody ConfirmEmailDTO dto) {
        EmailChangeToken stored = emailChangeTokenRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Link inválido ou já utilizado."
                ));

        if (stored.isExpired()) {
            emailChangeTokenRepository.delete(stored);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link expirado. Solicite a troca novamente.");
        }

        User user = stored.getUser();
        String newEmail = stored.getNewEmail();

        // Re-checa unicidade — outro usuário pode ter registrado esse email nesse meio-tempo
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está em uso.");
            }
        });

        user.setEmail(newEmail);
        userRepository.save(user);
        emailChangeTokenRepository.delete(stored);

        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new AuthResponseDTO(accessToken, refreshToken, new UserDTO(user));
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }
}
