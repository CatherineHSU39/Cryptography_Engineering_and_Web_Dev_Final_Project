package com.nycu.ce.ciphergame.backend.service.audit;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Audit;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.AuditRepository;
import com.nycu.ce.ciphergame.backend.util.CRUDAction;
import com.nycu.ce.ciphergame.backend.util.EntityType;

@Service
public class AuditService {

    @Autowired
    AuditRepository auditRepository;

    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }

    public void insertAudit(
            UserId userId,
            CRUDAction action,
            User userBefore,
            User userAfter
    ) {
        if (userBefore == null || userAfter == null) {
            return;
        }

        UserId entityId = userId;

        if (!userBefore.getUsername().equals(userAfter.getUsername())) {
            auditRepository.save(Audit.builder()
                    .userId(userId.toUUID())
                    .action(action.name())
                    .entityType(EntityType.USER.name())
                    .entityId(entityId.toUUID())
                    .columnName("username")
                    .beforeValue(userBefore.getUsername())
                    .afterValue(userAfter.getUsername())
                    .build());
        }
    }

    public void insertAudit(
            UserId userId,
            CRUDAction action,
            GroupId groupId,
            Group groupBefore,
            Group groupAfter
    ) {
        if (groupBefore == null || groupAfter == null) {
            return;
        }

        GroupId entityId = groupId;

        if (!groupBefore.getName().equals(groupAfter.getName())) {
            auditRepository.save(Audit.builder()
                    .userId(userId.toUUID())
                    .action(action.name())
                    .entityType(EntityType.GROUP.name())
                    .entityId(entityId.toUUID())
                    .columnName("name")
                    .beforeValue(groupBefore.getName())
                    .afterValue(groupAfter.getName())
                    .build());
        }
    }

    public void insertAudit(
            UserId userId,
            CRUDAction action,
            GroupId groupId,
            UserId memberIdBefore,
            UserId memberIdAfter
    ) {
        GroupId entityId = groupId;

        if (!memberIdBefore.equals(memberIdAfter)) {
            auditRepository.save(Audit.builder()
                    .userId(userId.toUUID())
                    .action(action.name())
                    .entityType(EntityType.GROUP.name())
                    .entityId(entityId.toUUID())
                    .columnName("member")
                    .beforeValue(memberIdBefore.toString())
                    .afterValue(memberIdAfter.toString())
                    .build());
        }
    }

    public void insertAudit(
            UserId userId,
            CRUDAction action,
            MessageId messageId,
            Message messageBefore,
            Message messageAfter
    ) {
        if (messageBefore == null || messageAfter == null) {
            return;
        }

        MessageId entityId = messageId;

        if (!messageBefore.getContent().equals(messageAfter.getContent())) {
            auditRepository.save(Audit.builder()
                    .userId(userId.toUUID())
                    .action(action.name())
                    .entityType(EntityType.MESSAGE.name())
                    .entityId(entityId.toUUID())
                    .columnName("encrypted_message")
                    .beforeValue(messageBefore.getContent())
                    .afterValue(messageAfter.getContent())
                    .build());
        }
    }
}
