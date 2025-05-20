package com.nycu.ce.ciphergame.backend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.message.MessageRequest;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.service.MessageService;

@RestController
@RequestMapping("/app/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable UUID id) {
        MessageResponse message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageRequest messageRequest) {
        MessageResponse message = messageService.createMessage(messageRequest);
        if (message == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(message);
    }
}
