package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;
import com.nycu.ce.ciphergame.backend.repository.GroupRepository;
import com.nycu.ce.ciphergame.backend.repository.MessageRepository;
import com.nycu.ce.ciphergame.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageMapper messageMapper;

    public MessageResponse getMessageById(UUID messageId) {
        return messageRepository.findById(messageId)
                .map(messageMapper::toDTO)
                .orElse(null);
    }

    public List<MessageResponse> getAllMessages(UUID groupId) {
        return messageRepository.findAllByGroupId(groupId)
                .stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse createMessage(UUID senderId, UUID groupId, MessageRequest dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Message newMessage = new Message();
        newMessage.setEncryptedMessage(dto.getEncryptedMessage());
        newMessage.setSender(sender);
        newMessage.setGroup(group);

        messageRepository.save(newMessage);
        return messageMapper.toDTO(newMessage);
    }

    public MessageResponse updateMessage(UUID messageId, MessageRequest dto) {
        Message targetMessage = messageRepository.findById(messageId).orElse(null);

        if (dto.getEncryptedMessage() != null) {
            targetMessage.setEncryptedMessage(dto.getEncryptedMessage());
        }
        messageRepository.save(targetMessage);
        return messageMapper.toDTO(targetMessage);
        
    }

    public boolean isMessageInGroup(UUID messageId, UUID groupId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message == null) {
            return false;
        }
        return message.getGroup().getId().equals(groupId);
    }
}
