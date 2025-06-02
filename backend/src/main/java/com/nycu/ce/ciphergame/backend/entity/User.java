package com.nycu.ce.ciphergame.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Entity
@Table(name = "backend_users")
public class User {

    @Id
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Member> memberships = new ArrayList<>();

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

    public User(String username) {
        this.username = username;
    }

    public User(UUID id) {
        this.id = id;
    }
}
