package com.nycu.ce.ciphergame.dek.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.dek.entity.Dek;

@Repository
public interface DekRepository extends JpaRepository<Dek, UUID> {

    @Query("""
    SELECT d
    FROM Dek d
    WHERE d.ownerId = :ownerId
""")
    Page<Dek> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Dek findByOwnerIdAndId(UUID ownerId, UUID dekId);

    @Query("""
    SELECT d
    FROM Dek d
    WHERE d.ownerId = :ownerId
    AND d.createdAt >= :timestamp
""")
    Page<Dek> findAllByOwnerIdSinceCreatedAt(
            @Param("timestamp") LocalDateTime timestamp,
            @Param("ownerId") UUID ownerId,
            Pageable pageable
    );
}
