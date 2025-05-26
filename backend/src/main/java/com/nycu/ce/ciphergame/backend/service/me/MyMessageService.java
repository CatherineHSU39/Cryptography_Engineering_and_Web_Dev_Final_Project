package com.nycu.ce.ciphergame.backend.service.me;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.Recipient;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.RecipientId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.MessageRepository;
import com.nycu.ce.ciphergame.backend.service.MemberService;
import com.nycu.ce.ciphergame.backend.service.MessageService;
import com.nycu.ce.ciphergame.backend.service.RecipientService;
import com.nycu.ce.ciphergame.backend.util.MessageStatus;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MyMessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MemberService memberService;

    public List<Message> getMyNewMessages(
            UserId userId
    ) {
        List<Recipient> recipients = recipientService.getAllRecipients(
                userId,
                MessageStatus.NEW
        );

        recipientService.updateRecipients(
                MessageStatus.UNREAD,
                recipients
        );

        List<UUID> messageIds = recipients.stream()
                .map(Recipient::getId)
                .map(RecipientId::getMessageId)
                .collect(Collectors.toList());

        return messageRepository.findAllById(messageIds);
    }

    public void markMyReadMessages(
            UserId userId,
            List<GroupId> groupIds
    ) {

        List<Recipient> recipients = recipientService.getAllRecipients(
                userId,
                MessageStatus.UNREAD
        );
        List<Recipient> targetRecipients = recipients.stream()
                .filter(recipient
                        -> groupIds.contains(GroupId.fromUUID(recipient.getGroupId())))
                .collect(Collectors.toList());

        recipientService.updateRecipients(MessageStatus.READ, targetRecipients);

    }

    public List<Message> createMyMessage(
            UserId userId,
            List<GroupId> groupIds,
            String content
    ) {
        memberService.validateAllMembers(groupIds, userId);

        List<Message> messages = groupIds.stream()
                .map(groupId -> messageService
                .createMessage(userId, groupId, content))
                .collect(Collectors.toList());
        return messages;
    }

    public Message updateMyMessage(
            UserId userId,
            MessageId messageId,
            String content
    ) {
        Message message = messageService.getMessageById(messageId);
        UUID senderId = message.getSender().getId();
        this.validateSender(userId, UserId.fromUUID(senderId));
        message.setContent(content);
        messageRepository.save(message);

        return message;
    }

    public Void deleteMyMessage(UserId userId, MessageId messageId) {
        Message message = messageService.getMessageById(messageId);
        UUID senderId = message.getSender().getId();
        this.validateSender(userId, UserId.fromUUID(senderId));
        messageRepository.delete(message);
        return null;
    }

    void validateSender(UserId userId, UserId senderId) {
        if (senderId != userId) {
            throw new RuntimeException("Sender invalid");
        }
    }
}
