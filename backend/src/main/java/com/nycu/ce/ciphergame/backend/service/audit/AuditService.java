package com.nycu.ce.ciphergame.backend.service.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Audit;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
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
            UUID userId,
            CRUDAction action,
            User userBefore,
            User userAfter
    ) {
        if (userBefore == null || userAfter == null) {
            return;
        }

        UUID entityId = userId;

        if (!userBefore.getUsername().equals(userAfter.getUsername())) {
            auditRepository.save(Audit.builder()
                    .userId(userId)
                    .action(action.name())
                    .entityType(EntityType.USER.name())
                    .entityId(entityId)
                    .columnName("username")
                    .beforeValue(userBefore.getUsername())
                    .afterValue(userAfter.getUsername())
                    .build());
        }
    }

    public void insertAudit(
            UUID userId,
            CRUDAction action,
            UUID groupId,
            Group groupBefore,
            Group groupAfter
    ) {
        if (groupBefore == null || groupAfter == null) {
            return;
        }

        UUID entityId = groupId;

        if (!groupBefore.getName().equals(groupAfter.getName())) {
            auditRepository.save(Audit.builder()
                    .userId(userId)
                    .action(action.name())
                    .entityType(EntityType.GROUP.name())
                    .entityId(entityId)
                    .columnName("name")
                    .beforeValue(groupBefore.getName())
                    .afterValue(groupAfter.getName())
                    .build());
        }
    }

    public void insertAudit(
            UUID userId,
            CRUDAction action,
            UUID groupId,
            UUID memberIdBefore,
            UUID memberIdAfter
    ) {
        UUID entityId = groupId;

        if (!memberIdBefore.equals(memberIdAfter)) {
            auditRepository.save(Audit.builder()
                    .userId(userId)
                    .action(action.name())
                    .entityType(EntityType.GROUP.name())
                    .entityId(entityId)
                    .columnName("member")
                    .beforeValue(memberIdBefore.toString())
                    .afterValue(memberIdAfter.toString())
                    .build());
        }
    }

    public void insertAudit(
            UUID userId,
            CRUDAction action,
            UUID messageId,
            Message messageBefore,
            Message messageAfter
    ) {
        if (messageBefore == null || messageAfter == null) {
            return;
        }

        UUID entityId = messageId;

        if (!messageBefore.getEncryptedMessage().equals(messageAfter.getEncryptedMessage())) {
            auditRepository.save(Audit.builder()
                    .userId(userId)
                    .action(action.name())
                    .entityType(EntityType.MESSAGE.name())
                    .entityId(entityId)
                    .columnName("encrypted_message")
                    .beforeValue(messageBefore.getEncryptedMessage())
                    .afterValue(messageAfter.getEncryptedMessage())
                    .build());
        }
    }
}
