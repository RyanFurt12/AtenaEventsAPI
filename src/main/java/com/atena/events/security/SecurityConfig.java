package com.atena.events.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthorizationRequestResolver customResolver;
    private final OAuthSuccessHandler oAuthSuccessHandler;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            CustomAuthorizationRequestResolver customResolver,
            OAuthSuccessHandler oAuthSuccessHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customResolver = customResolver;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            // OAuth2 dance needs a session; STATELESS applies to the JWT API paths only.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — public
                .requestMatchers("/auth/register", "/auth/login",
                                 "/auth/refresh",  "/auth/logout",
                                 "/auth/guest",
                                 "/auth/forgot-password", "/auth/reset-password",
                                 "/auth/confirm-email").permitAll()
                // OAuth2 flow — public (Spring Security OAuth2 Client endpoints)
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Public event reads
                .requestMatchers(HttpMethod.GET, "/events/recommended").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/{id}").permitAll()
                // Public comment reads
                .requestMatchers(HttpMethod.GET, "/comments/event/{eventId}").permitAll()
                // Public whiteboard read (POST/PUT/DELETE caem no anyRequest → ROLE_USER)
                .requestMatchers(HttpMethod.GET, "/events/{eventId}/whiteboard").permitAll()
                // Participation: guests and full users can toggle and check their own status
                .requestMatchers(HttpMethod.POST,
                    "/participate/toggle/event/{eventId}/user/{userId}")
                    .hasAnyRole("USER", "GUEST")
                .requestMatchers(HttpMethod.GET,
                    "/participate/event/{eventId}/user/{userId}")
                    .hasAnyRole("USER", "GUEST")
                // Account upgrade — requires active guest session
                .requestMatchers(HttpMethod.POST, "/auth/upgrade/password").hasRole("GUEST")
                // Everything else: full authenticated users only
                .anyRequest().hasRole("USER")
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(a -> a
                    .authorizationRequestResolver(customResolver))
                .successHandler(oAuthSuccessHandler)
                .failureHandler((req, res, ex) ->
                    res.sendRedirect(frontendUrl + "/signin?error=oauth_failed"))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
