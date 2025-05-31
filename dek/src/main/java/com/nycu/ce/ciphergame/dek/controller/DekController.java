package com.nycu.ce.ciphergame.dek.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.nycu.ce.ciphergame.dek.dto.DekQuery;
import com.nycu.ce.ciphergame.dek.dto.DekRequest;
import com.nycu.ce.ciphergame.dek.dto.DekResponse;
import com.nycu.ce.ciphergame.dek.entity.Dek;
import com.nycu.ce.ciphergame.dek.entity.id.DekId;
import com.nycu.ce.ciphergame.dek.entity.id.UserId;
import com.nycu.ce.ciphergame.dek.mapper.DekMapper;
import com.nycu.ce.ciphergame.dek.service.DekService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/deks")
public class DekController {

    @Autowired
    private DekService dekService;

    @Autowired
    private DekMapper dekMapper;

    @PostMapping
    public ResponseEntity<List<DekResponse>> storeDeks(
            @Valid @RequestBody List<DekRequest> requests
    ) {
        List<Dek> newDeks = dekService.storeDeks(dekMapper.toEntity(requests));
        return ResponseEntity.ok(dekMapper.toDTO(newDeks));
    }

    @GetMapping
    public ResponseEntity<List<DekResponse>> getDeks(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute DekQuery queries
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        List<Dek> deks = dekService.getDeks(
                myId,
                queries.getDekIds().stream()
                        .map(DekId::fromUUID)
                        .collect(Collectors.toList()));
        return ResponseEntity.ok(dekMapper.toDTO(deks));
    }
}
