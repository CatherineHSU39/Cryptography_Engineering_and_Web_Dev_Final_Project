package com.nycu.ce.ciphergame.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findAllByGroupId(UUID groupId, Pageable pageable);

    Page<Message> findAll(Pageable pageable);

    @Query("""
    SELECT m
    FROM Message m
    WHERE m.group.id IN :groupIds
    AND m.updatedAt > :timestamp
""")
    Page<Message> findAllSinceUpdatedAtGroupIdIn(
            @Param("timestamp") LocalDateTime timestamp,
            @Param("groupIds") List<UUID> groupIds,
            Pageable pageable
    );

}
