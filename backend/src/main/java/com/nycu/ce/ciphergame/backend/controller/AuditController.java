package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.audit.AuditResponse;
import com.nycu.ce.ciphergame.backend.mapper.AuditMapper;
import com.nycu.ce.ciphergame.backend.service.audit.AuditService;

@RestController
@RequestMapping("/app/audits")
public class AuditController {

    @Autowired
    AuditService auditService;

    @Autowired
    AuditMapper auditMapper;

    @GetMapping
    public ResponseEntity<List<AuditResponse>> getAllAudits() {
        List<AuditResponse> response = auditService.getAllAudits().stream()
                .map(auditMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
