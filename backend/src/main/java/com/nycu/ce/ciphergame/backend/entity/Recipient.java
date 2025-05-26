package com.nycu.ce.ciphergame.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.RecipientId;
import com.nycu.ce.ciphergame.backend.util.MessageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "message_recipients")
public class Recipient {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private RecipientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(
            name = "message_id",
            nullable = false,
            updatable = false
    )
    private Message message;

    @Column(
            name = "group_id",
            nullable = false,
            updatable = false
    )
    private UUID groupId;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(
            name = "sent_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime sentAt;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now();
        this.status = MessageStatus.NEW;
    }

    protected Recipient() {
    }

    public Recipient(
            User user,
            Message message,
            GroupId groupId
    ) {
        if (user.getId() == null
                || message.getId() == null
                || groupId == null) {
            throw new IllegalArgumentException("User ID or Message ID is null");
        }
        this.user = user;
        this.message = message;
        this.groupId = groupId.toUUID();
        this.id = new RecipientId(user.getId(), message.getId());
    }
}
