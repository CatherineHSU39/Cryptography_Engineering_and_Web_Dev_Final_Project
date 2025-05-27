package com.nycu.ce.ciphergame.backend.entity.id;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record GroupId(
        @NotNull
        UUID value
        ) implements Serializable {

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }

    public static GroupId fromString(String str) {
        return new GroupId(UUID.fromString(str));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    public static GroupId fromUUID(UUID id) {
        return new GroupId(id);
    }

    public UUID toUUID() {
        return value;
    }
}
