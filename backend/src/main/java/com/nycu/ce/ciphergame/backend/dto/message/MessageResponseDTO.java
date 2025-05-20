package com.nycu.ce.ciphergame.backend.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class MessageResponseDTO {
    private UUID messageId;
    private UUID groupId;
    private UUID senderId;
    private String encryptedMessage;
    private LocalDateTime createdAt;
}