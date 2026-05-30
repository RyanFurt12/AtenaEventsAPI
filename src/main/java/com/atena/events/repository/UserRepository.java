package com.atena.events.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atena.events.model.AccountType;
import com.atena.events.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByAccountTypeAndProviderId(AccountType accountType, String providerId);
}
