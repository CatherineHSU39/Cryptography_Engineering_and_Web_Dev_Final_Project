package com.nycu.ce.ciphergame.dek.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

import com.nimbusds.jose.util.Base64;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "encrypted_deks")
public class Dek {

    private static final Logger log = Logger.getLogger(Dek.class.getName());

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "cmk_version", nullable = false)
    private int cmkVersion;

    @Column(name = "nonce", columnDefinition = "BYTEA", nullable = false)
    private byte[] nonce;

    @Column(name = "encrypted_dek", columnDefinition = "BYTEA", nullable = false)
    private byte[] encrypted_dek;

    @Column(name = "aad", columnDefinition = "TEXT", nullable = false)
    private String aad;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.cmkVersion = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Dek() {
    }

    public Dek(
            UUID id,
            UUID ownerId,
            int cmkVersion,
            byte[] nonce,
            byte[] encrypted_dek,
            String aad,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.cmkVersion = cmkVersion;
        this.nonce = nonce;
        this.encrypted_dek = encrypted_dek;
        this.aad = aad;
        this.createdAt = createdAt;

        // Logging type and details of `dek`
        log.info("Type of dek: " + encrypted_dek.getClass().getName());
        log.info("Length of dek: " + encrypted_dek.length);
        log.info("Base64 of dek: " + Base64.encode(encrypted_dek).toString()); // Nimbus Base64 requires `.encode(...)`
    }
}
