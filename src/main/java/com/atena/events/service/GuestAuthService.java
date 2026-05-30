package com.atena.events.service;

import com.atena.events.model.AccountType;
import com.atena.events.model.Participation;
import com.atena.events.model.RefreshToken;
import com.atena.events.model.User;
import com.atena.events.model.dto.AuthResponseDTO;
import com.atena.events.model.dto.GuestCreateDTO;
import com.atena.events.model.dto.UpgradePasswordDTO;
import com.atena.events.model.dto.UserDTO;
import com.atena.events.repository.ParticipationRepository;
import com.atena.events.repository.RefreshTokenRepository;
import com.atena.events.repository.UserRepository;
import com.atena.events.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GuestAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ParticipationRepository participationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    public GuestAuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            ParticipationRepository participationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.participationRepository = participationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDTO createGuest(GuestCreateDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Este username já está em uso.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setName(dto.getUsername());
        user.setAccountType(AccountType.GUEST);
        userRepository.save(user);

        // Guests get a short-lived access token and no refresh token
        String token = jwtService.generateAccessToken(user);
        return new AuthResponseDTO(token, null, new UserDTO(user));
    }

    @Transactional
    public AuthResponseDTO upgradeWithPassword(Long guestUserId, UpgradePasswordDTO dto) {
        User user = userRepository.findById(guestUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Usuário não encontrado."));

        if (user.getAccountType() != AccountType.GUEST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Conta já é completa.");
        }

        // Check email uniqueness
        userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(guestUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este email já está em uso.");
            }
        });

        // Upgrade in place — same user ID, so all participations are preserved via FK
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAccountType(AccountType.PASSWORD);
        user.setUpgradedAt(Instant.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Re-assigns the guest's participations to targetUser, then deletes the guest.
     * Safe to call even if the guest no longer exists (idempotent).
     */
    @Transactional
    public void mergeGuestIntoUser(Long targetUserId, Long guestId) {
        userRepository.findById(guestId).ifPresent(guest -> {
            if (guest.getAccountType() != AccountType.GUEST) return;

            User target = userRepository.getReferenceById(targetUserId);
            List<Participation> guestParticipations = participationRepository.findByUserId(guestId);

            for (Participation p : guestParticipations) {
                boolean alreadyJoined = participationRepository
                    .findByUserIdAndEventId(targetUserId, p.getEvent().getId()).isPresent();
                if (!alreadyJoined) {
                    p.setUser(target);
                    participationRepository.save(p);
                }
            }

            // Flush reassignments before deleting so the cascade only removes duplicates
            userRepository.flush();
            userRepository.delete(guest);
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public AuthResponseDTO buildAuthResponse(User user) {
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
