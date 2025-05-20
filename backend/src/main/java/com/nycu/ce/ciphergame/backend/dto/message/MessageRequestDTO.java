package com.nycu.ce.ciphergame.backend.dto.message;

import java.util.UUID;

import lombok.Data;

@Data
public class MessageRequestDTO {
    private UUID senderId;
    private UUID groupId;
    private String encryptedMessage;
}
