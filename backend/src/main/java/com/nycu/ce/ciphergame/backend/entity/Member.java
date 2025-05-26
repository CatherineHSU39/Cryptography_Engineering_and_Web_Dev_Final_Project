package com.nycu.ce.ciphergame.backend.entity;

import java.time.LocalDateTime;

import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MemberId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "group_members")
public class Member {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private MemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(
            name = "joined_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime joinedAt;

    @PrePersist
    public void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    protected Member() {
    }

    public Member(User user, Group group) {
        if (user.getId() == null || group.getId() == null) {
            throw new IllegalArgumentException("User ID or Group ID is null");
        }
        this.user = user;
        this.group = group;
        this.id = new MemberId(user.getId(), group.getId());
    }
}
