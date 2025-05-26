package com.nycu.ce.ciphergame.backend.entity.id;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RecipientId implements Serializable {

    private UUID userId;
    private UUID messageId;

    // equals + hashCode (important!)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientId)) {
            return false;
        }
        RecipientId that = (RecipientId) o;
        return userId.equals(that.userId) && messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, messageId);
    }
}
