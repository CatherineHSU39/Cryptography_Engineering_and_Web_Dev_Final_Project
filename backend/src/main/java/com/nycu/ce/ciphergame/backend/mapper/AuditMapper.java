package com.nycu.ce.ciphergame.backend.mapper;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.audit.AuditResponse;
import com.nycu.ce.ciphergame.backend.entity.Audit;

@Component
public class AuditMapper {

    public AuditResponse toDTO(Audit entity) {
        return AuditResponse.builder()
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUserId())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .columnName(entity.getColumnName())
                .beforeValue(entity.getBeforeValue())
                .afterValue(entity.getAfterValue())
                .build();
    }
}
