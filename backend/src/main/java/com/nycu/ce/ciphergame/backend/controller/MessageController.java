package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.service.GroupService;
import com.nycu.ce.ciphergame.backend.service.MessageService;

@RestController
@RequestMapping("/app/groups/{groupId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private GroupService groupService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId
    ) {
        UUID senderId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(senderId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        List<MessageResponse> messages = messageService.getAllMessages(groupId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessageById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId
    ) {
        UUID senderId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(senderId, groupId)
                || !messageService.isMessageInGroup(messageId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        MessageResponse message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(message);
    }

    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @RequestBody MessageRequest messageRequest
    ) {
        UUID senderId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(senderId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        MessageResponse message = messageService.createMessage(senderId, groupId, messageRequest);
        return ResponseEntity.ok(message);
    }
}
