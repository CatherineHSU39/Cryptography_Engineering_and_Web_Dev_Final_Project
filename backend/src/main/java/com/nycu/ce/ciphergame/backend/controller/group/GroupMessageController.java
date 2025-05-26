package com.nycu.ce.ciphergame.backend.controller.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.message.MessageQuery;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;
import com.nycu.ce.ciphergame.backend.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/groups/{groupId}/messages")
public class GroupMessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable GroupId groupId,
            @Valid @ModelAttribute MessageQuery messageQuery
    ) {
        Page<Message> messages = messageService.getMessagesByGroupId(
                groupId,
                messageQuery.toPageable()
        );
        return ResponseEntity.ok(messageMapper.toDTO(messages));
    }
}
