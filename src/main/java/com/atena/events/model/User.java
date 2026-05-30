package com.atena.events.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_provider", columnNames = {"account_type", "provider_id"})
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Nullable — guests have no email; set by password/OAuth users
    @Column(unique = true)
    private String email;

    // Nullable — guests and OAuth users have no password
    private String password;

    @Column(columnDefinition = "TEXT")
    private String avatarBase64;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType = AccountType.PASSWORD;

    // Unique display handle — required for guests, auto-generated for OAuth users
    @Column(unique = true, length = 50)
    private String username;

    // OAuth provider user ID (e.g. Google subject, GitHub node_id)
    @Column(length = 255)
    private String providerId;

    // Profile photo URL from the OAuth provider (Google picture / GitHub avatar_url)
    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant upgradedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Event> createdEvents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Participation> participations;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comments;

    // ── UserDetails ──────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (accountType == AccountType.GUEST) {
            return List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // Returns the raw username handle (distinct from UserDetails.getUsername())
    public String getHandle() {
        return username;
    }

    @Override
    public String getUsername() {
        return email != null ? email : username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
