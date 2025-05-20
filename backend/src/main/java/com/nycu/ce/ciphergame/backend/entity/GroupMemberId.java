package com.nycu.ce.ciphergame.backend.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Embeddable
public class GroupMemberId implements Serializable {
    private UUID userId;
    private UUID groupId;

    // equals + hashCode (important!)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMemberId)) return false;
        GroupMemberId that = (GroupMemberId) o;
        return userId.equals(that.userId) && groupId.equals(that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }
}