package com.nycu.ce.ciphergame.dek.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.dek.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
