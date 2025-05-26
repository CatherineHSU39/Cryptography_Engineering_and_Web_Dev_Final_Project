package com.nycu.ce.ciphergame.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.message.MessageQuery;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;
import com.nycu.ce.ciphergame.backend.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    MessageMapper messageMapper;

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getAllMessages(
            @Valid @ModelAttribute MessageQuery messageQuery
    ) {
        Pageable pageable = messageQuery.toPageable();
        Page<Message> message = messageService.getAllMessages(pageable);
        return ResponseEntity.ok(messageMapper.toDTO(message));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable MessageId messageId
    ) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }
}
