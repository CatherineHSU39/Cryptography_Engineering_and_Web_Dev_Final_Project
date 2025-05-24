package com.nycu.ce.ciphergame.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Audit;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

}
