package com.nycu.ce.ciphergame.backend.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {

    private UUID messageId;
    private UUID groupId;
    private UUID senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
