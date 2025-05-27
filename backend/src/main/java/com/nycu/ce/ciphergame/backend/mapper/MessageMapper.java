package com.nycu.ce.ciphergame.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
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
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    public Page<MessageResponse> toDTO(Page<Message> messages) {
        return messages.map(this::toDTO);
    }

    public List<MessageResponse> toDTO(List<Message> messages) {
        return messages.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
