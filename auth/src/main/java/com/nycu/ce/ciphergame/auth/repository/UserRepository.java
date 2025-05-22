package com.nycu.ce.ciphergame.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    User findByUsername(String username);
}
