package com.nycu.ce.ciphergame.backend.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.repository.MessageRepository;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    public MessageResponse getMessageById(UUID id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDTO)
                .orElse(null);
    }

    public MessageResponse createMessage(MessageRequest messageRequestDTO) {
        Message message = messageMapper.toEntity(messageRequestDTO);
        message = messageRepository.save(message);
        return messageMapper.toDTO(message);
    }
}
