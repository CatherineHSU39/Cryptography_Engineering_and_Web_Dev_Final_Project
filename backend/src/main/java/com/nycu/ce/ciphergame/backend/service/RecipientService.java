package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.Recipient;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.RecipientId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.RecipientRepository;
import com.nycu.ce.ciphergame.backend.util.MessageStatus;

@Service
public class RecipientService {

    @Autowired
    RecipientRepository recipientRepository;

    public List<Recipient> getAllRecipients(
            UserId userId,
            MessageStatus status
    ) {
        return recipientRepository.findAllByUserIdAndStatus(
                userId.toUUID(),
                status
        );
    }

    public Page<Recipient> getAllRecipients(
            UserId userId,
            MessageStatus status,
            Pageable pageable
    ) {
        return recipientRepository.findAllByUserIdAndStatus(
                userId.toUUID(),
                status,
                pageable
        );
    }

    public void updateRecipients(
            MessageStatus status,
            List<Recipient> recipients
    ) {
        recipients.forEach(recipient -> recipient.setStatus(status));

        recipientRepository.saveAll(recipients);
    }

    public void addAllRecipients(Message message, List<User> users, GroupId groupId) {
        Set<Recipient> oldRecipients = message.getRecipients();

        Set<Recipient> newRecipients = users.stream()
                .map(user -> new Recipient(user, message, groupId))
                .filter(member -> !oldRecipients.contains(member))
                .collect(Collectors.toSet());

        recipientRepository.saveAll(newRecipients);
    }

    public Recipient getRecipient(MessageId messageId, UserId userId) {
        return recipientRepository.findById(RecipientId.builder()
                .userId(userId.toUUID())
                .messageId(messageId.toUUID())
                .build())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
    }
}
