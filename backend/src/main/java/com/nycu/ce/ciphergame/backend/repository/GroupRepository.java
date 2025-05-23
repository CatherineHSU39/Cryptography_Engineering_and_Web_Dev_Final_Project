package com.nycu.ce.ciphergame.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
}
