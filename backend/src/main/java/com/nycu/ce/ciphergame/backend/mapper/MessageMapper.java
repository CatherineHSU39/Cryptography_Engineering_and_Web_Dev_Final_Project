package com.nycu.ce.ciphergame.backend.mapper;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;

@Component
public class MessageMapper {

    public MessageResponse toDTO(Message message) {

        return MessageResponse.builder()
                .messageId(message.getId())
                .groupId(message.getGroup().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getUsername())
                .encryptedMessage(message.getEncryptedMessage())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
