package com.nycu.ce.ciphergame.dek.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "encrypted_deks")
public class Dek {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "cmk_version")
    private int cmkVersion;

    @Column(name = "encrypted_dek")
    private String dek;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.cmkVersion = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Dek() {
    }
}
