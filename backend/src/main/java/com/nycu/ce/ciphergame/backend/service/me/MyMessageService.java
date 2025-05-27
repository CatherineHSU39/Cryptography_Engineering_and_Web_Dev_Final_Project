package com.nycu.ce.ciphergame.backend.service.me;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.MessageRepository;
import com.nycu.ce.ciphergame.backend.service.MemberService;
import com.nycu.ce.ciphergame.backend.service.MessageService;
import com.nycu.ce.ciphergame.backend.service.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MyMessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    public Page<Message> getMyNewMessages(UserId userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        LocalDateTime fetchNewAt = user.getFetchNewAt();

        List<UUID> groupIds = user.getMemberships().stream()
                .map(m -> m.getId().getGroupId())
                .toList();

        if (groupIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Message> page = messageRepository.findAllSinceCreatedAtGroupIdIn(
                fetchNewAt,
                groupIds,
                pageable
        );

        page.getContent().stream().findFirst()
                .map(Message::getCreatedAt)
                .ifPresent(user::setFetchNewAt);

        return page;
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
