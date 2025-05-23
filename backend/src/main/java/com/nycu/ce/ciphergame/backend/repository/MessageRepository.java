package com.nycu.ce.ciphergame.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId")
    List<Message> findAllByGroupId(UUID groupId);
}
