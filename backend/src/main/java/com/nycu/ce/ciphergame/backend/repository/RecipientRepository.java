package com.nycu.ce.ciphergame.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Recipient;
import com.nycu.ce.ciphergame.backend.entity.id.RecipientId;
import com.nycu.ce.ciphergame.backend.util.MessageStatus;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, RecipientId> {

    @Query("""
        SELECT r
        FROM Recipient r
        WHERE r.user.id = :userId
        AND r.status = :status
    """)
    Page<Recipient> findAllByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") MessageStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM Recipient r
        WHERE r.user.id = :userId
        AND r.status = :status
    """)
    List<Recipient> findAllByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") MessageStatus status
    );
}
