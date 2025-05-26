package com.nycu.ce.ciphergame.backend.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findAllByGroupId(UUID groupId, Pageable pageable);

    Page<Message> findAll(Pageable pageable);
}
