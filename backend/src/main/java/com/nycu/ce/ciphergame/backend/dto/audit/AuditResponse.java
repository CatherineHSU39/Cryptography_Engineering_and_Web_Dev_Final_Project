package com.nycu.ce.ciphergame.backend.dto.audit;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuditResponse {

    private LocalDateTime createdAt;
    private UUID userId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String columnName;
    private String beforeValue;
    private String afterValue;
}
