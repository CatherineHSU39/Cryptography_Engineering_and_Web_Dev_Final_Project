package com.nycu.ce.ciphergame.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.MessageRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RecipientService recipientService;

    public Message getMessageById(MessageId messageId) {
        return messageRepository.findById(messageId.toUUID())
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public Page<Message> getAllMessages(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    public Page<Message> getMessagesByGroupId(
            GroupId groupId,
            Pageable pageable
    ) {
        return messageRepository.findAllByGroupId(groupId.toUUID(), pageable);
    }

    public Message createMessage(UserId senderId, GroupId groupId, String content) {
        Group group = groupService.getGroupById(groupId);
        User sender = userService.getUserById(senderId);
        Message newMessage = new Message(sender, group, content);

        messageRepository.save(newMessage);

        List<User> reciviers = memberService.getAllUsersByGroupId(groupId);
        recipientService.addAllRecipients(newMessage, reciviers, groupId);
        return newMessage;
    }

    public Message updateMessage(MessageId messageId, String content) {
        Message targetMessage = messageRepository.findById(messageId.toUUID())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        targetMessage.setContent(content);
        messageRepository.save(targetMessage);
        return targetMessage;

    }

    public Void deleteMessage(MessageId messageId) {
        messageRepository.deleteById(messageId.toUUID());
        return null;
    }
}
