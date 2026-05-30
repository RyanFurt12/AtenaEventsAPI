package com.atena.events.security;

import com.atena.events.model.AccountType;
import com.atena.events.model.User;
import com.atena.events.model.dto.AuthResponseDTO;
import com.atena.events.repository.UserRepository;
import com.atena.events.service.GuestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final GuestAuthService guestAuthService;

    public OAuthSuccessHandler(UserRepository userRepository, GuestAuthService guestAuthService) {
        this.userRepository = userRepository;
        this.guestAuthService = guestAuthService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId(); // "google" or "github"

        AccountType provider = AccountType.valueOf(registrationId.toUpperCase());
        String providerId = resolveProviderId(oauthUser, registrationId);
        String email      = resolveEmail(oauthUser, registrationId);
        String name       = resolveName(oauthUser);
        String avatarUrl  = resolveAvatarUrl(oauthUser, registrationId);

        // ── Case 1: Returning OAuth user ─────────────────────────────────────
        Optional<User> byProvider = userRepository.findByAccountTypeAndProviderId(provider, providerId);
        if (byProvider.isPresent()) {
            User returning = byProvider.get();
            // Refresh avatar URL from provider (user may have changed their photo)
            if (returning.getAvatarBase64() == null && avatarUrl != null
                    && !avatarUrl.equals(returning.getAvatarUrl())) {
                returning.setAvatarUrl(avatarUrl);
                userRepository.save(returning);
            }
            redirectWithTokens(response, returning);
            return;
        }

        // ── Case 2: Guest upgrading via OAuth ────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session != null) {
            String guestIdStr = (String) session.getAttribute(CustomAuthorizationRequestResolver.SESSION_KEY);
            if (guestIdStr != null) {
                session.removeAttribute(CustomAuthorizationRequestResolver.SESSION_KEY);
                try {
                    Long guestId = Long.parseLong(guestIdStr);
                    Optional<User> guestOpt = userRepository.findById(guestId);
                    if (guestOpt.isPresent() && guestOpt.get().getAccountType() == AccountType.GUEST) {
                        User guest = guestOpt.get();
                        guest.setEmail(email);
                        guest.setName(name != null ? name : guest.getName());
                        guest.setProviderId(providerId);
                        guest.setAccountType(provider);
                        guest.setAvatarUrl(avatarUrl);
                        guest.setUpgradedAt(Instant.now());
                        userRepository.save(guest);
                        redirectWithTokens(response, guest);
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // ── Case 3: Existing email match — link provider to existing account ─
        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                User existing = byEmail.get();
                existing.setProviderId(providerId);
                existing.setAccountType(provider);
                // Only set avatarUrl if the user hasn't uploaded a custom avatar
                if (existing.getAvatarBase64() == null) {
                    existing.setAvatarUrl(avatarUrl);
                }
                userRepository.save(existing);
                redirectWithTokens(response, existing);
                return;
            }
        }

        // ── Case 4: Brand-new user via OAuth ─────────────────────────────────
        User newUser = new User();
        newUser.setName(name != null ? name : registrationId + "_user");
        newUser.setEmail(email);
        newUser.setProviderId(providerId);
        newUser.setAccountType(provider);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setUsername(generateUniqueUsername(name != null ? name : registrationId));
        userRepository.save(newUser);
        redirectWithTokens(response, newUser);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void redirectWithTokens(HttpServletResponse response, User user) throws IOException {
        AuthResponseDTO auth = guestAuthService.buildAuthResponse(user);
        String url = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-callback")
            .queryParam("accessToken", auth.getAccessToken())
            .queryParam("refreshToken", auth.getRefreshToken())
            .build().toUriString();
        response.sendRedirect(url);
    }

    private String resolveProviderId(OAuth2User user, String registrationId) {
        Map<String, Object> attrs = user.getAttributes();
        if ("github".equals(registrationId)) {
            Object id = attrs.get("id");
            return id != null ? String.valueOf(id) : null;
        }
        // Google: "sub" claim
        Object sub = attrs.get("sub");
        return sub != null ? String.valueOf(sub) : null;
    }

    private String resolveEmail(OAuth2User user, String registrationId) {
        Map<String, Object> attrs = user.getAttributes();
        if ("github".equals(registrationId)) {
            Object email = attrs.get("email");
            return email != null ? String.valueOf(email) : null;
        }
        Object email = attrs.get("email");
        return email != null ? String.valueOf(email) : null;
    }

    private String resolveName(OAuth2User user) {
        Map<String, Object> attrs = user.getAttributes();
        Object name = attrs.get("name");
        if (name != null) return String.valueOf(name);
        Object login = attrs.get("login"); // GitHub username field
        return login != null ? String.valueOf(login) : null;
    }

    private String resolveAvatarUrl(OAuth2User user, String registrationId) {
        Map<String, Object> attrs = user.getAttributes();
        // Google: "picture" | GitHub: "avatar_url"
        String key = "github".equals(registrationId) ? "avatar_url" : "picture";
        Object url = attrs.get(key);
        return url != null ? String.valueOf(url) : null;
    }

    private String generateUniqueUsername(String displayName) {
        String base = displayName.toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^a-z0-9_\\-]", "");
        if (base.isBlank()) base = "user";
        if (base.length() > 20) base = base.substring(0, 20);

        String candidate = base;
        Random rng = new Random();
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + "_" + (1000 + rng.nextInt(9000));
        }
        return candidate;
    }
}
