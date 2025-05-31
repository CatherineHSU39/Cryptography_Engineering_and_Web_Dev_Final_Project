package com.nycu.ce.ciphergame.dek.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.dek.entity.Dek;

@Repository
public interface DekRepository extends JpaRepository<Dek, UUID> {

    @Query("""
    SELECT d
    FROM Dek d
    WHERE d.ownerId = :ownerId
    AND d.id IN :dekIds
""")
    List<Dek> findAllOwnerIdAndIdIn(UUID ownerId, List<UUID> dekIds);
}
