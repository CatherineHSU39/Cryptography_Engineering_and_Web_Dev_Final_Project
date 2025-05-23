package com.nycu.ce.ciphergame.backend.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;

@Component
public class MessageMapper {

    public MessageResponse toDTO(Message message) {
        MessageResponse response = new MessageResponse();
        UUID senderId = message.getSender() != null ? message.getSender().getId() : null;
        UUID groupId = message.getGroup() != null ? message.getGroup().getId() : null;
        System.out.println(">>> MAPPING message: senderId=" + senderId + ", groupId=" + groupId);

        response.setMessageId(message.getId());
        response.setEncryptedMessage(message.getEncryptedMessage());
        response.setGroupId(message.getGroup().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getUsername());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
