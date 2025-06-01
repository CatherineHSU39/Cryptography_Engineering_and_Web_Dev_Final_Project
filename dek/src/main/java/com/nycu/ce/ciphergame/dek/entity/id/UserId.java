package com.nycu.ce.ciphergame.dek.entity.id;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record UserId(
        @NotNull
        UUID value
        ) implements Serializable {

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId fromString(String str) {
        return new UserId(UUID.fromString(str));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    public static UserId fromUUID(UUID id) {
        return new UserId(id);
    }

    public UUID toUUID() {
        return value;
    }
}
