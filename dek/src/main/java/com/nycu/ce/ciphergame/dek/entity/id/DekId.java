package com.nycu.ce.ciphergame.dek.entity.id;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record DekId(
        @NotNull
        UUID value
        ) implements Serializable {

    public static DekId generate() {
        return new DekId(UUID.randomUUID());
    }

    public static DekId fromString(String str) {
        return new DekId(UUID.fromString(str));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    public static DekId fromUUID(UUID id) {
        return new DekId(id);
    }

    public UUID toUUID() {
        return value;
    }
}
