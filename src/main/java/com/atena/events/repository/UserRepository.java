package com.atena.events.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.atena.events.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
