package com.nycu.ce.ciphergame.backend.entity.id;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record MessageId(
        @NotNull
        UUID value
        ) implements Serializable {

    public static MessageId generate() {
        return new MessageId(UUID.randomUUID());
    }

    public static MessageId fromString(String str) {
        return new MessageId(UUID.fromString(str));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    public static MessageId fromUUID(UUID id) {
        return new MessageId(id);
    }

    public UUID toUUID() {
        return value;
    }
}
