package com.nycu.ce.ciphergame.dek.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Entity
@Table(name = "dek_users")
public class User {

    @Id
    private UUID id;

    @ToString.Include
    @Column(
            name = "fetch_new_at",
            nullable = false
    )
    private LocalDateTime fetchNewAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.fetchNewAt = LocalDateTime.now();
    }

    protected User() {
    }

    public User(UUID id) {
        this.id = id;
    }
}
