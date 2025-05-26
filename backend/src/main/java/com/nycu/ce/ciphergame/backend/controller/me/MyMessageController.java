package com.nycu.ce.ciphergame.backend.controller.me;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.dto.recipient.RecipientRequest;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;
import com.nycu.ce.ciphergame.backend.service.me.MyMessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/me/messages")
public class MyMessageController {

    @Autowired
    private MyMessageService myMessageService;

    @Autowired
    private MessageMapper messageMapper;

    @PostMapping
    public ResponseEntity<List<MessageResponse>> createMessage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MessageRequest messageRequest
    ) {
        UserId senderId = UserId.fromString(jwt.getSubject());
        List<Message> messages = myMessageService.createMyMessage(
                senderId,
                messageRequest.getGroupIds(),
                messageRequest.getContent()
        );
        return ResponseEntity.ok(messageMapper.toDTO(messages));
    }

    @GetMapping("/new")
    public ResponseEntity<List<MessageResponse>> getNewMessages(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId senderId = UserId.fromString(jwt.getSubject());
        List<Message> messages = myMessageService.getMyNewMessages(
                senderId
        );
        return ResponseEntity.ok(messageMapper.toDTO(messages));
    }

    @PutMapping("/read")
    public ResponseEntity<Void> markReadMessages(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RecipientRequest recipientRequest
    ) {
        UserId senderId = UserId.fromString(jwt.getSubject());
        myMessageService.markMyReadMessages(
                senderId,
                recipientRequest.getGroupIds()
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable MessageId messageId,
            @Valid @RequestBody MessageRequest messageRequest
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        Message message = myMessageService.updateMyMessage(
                userId,
                messageId,
                messageRequest.getContent()
        );
        return ResponseEntity.ok(messageMapper.toDTO(message));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable MessageId messageId
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        myMessageService.deleteMyMessage(
                userId,
                messageId
        );
        return ResponseEntity.ok().build();
    }
}
