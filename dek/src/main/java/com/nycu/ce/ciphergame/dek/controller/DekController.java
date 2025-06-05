package com.nycu.ce.ciphergame.dek.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.dek.dto.DekDTO;
import com.nycu.ce.ciphergame.dek.dto.DekQuery;
import com.nycu.ce.ciphergame.dek.entity.Dek;
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
    public ResponseEntity<List<DekDTO>> storeDeks(
            @Valid @RequestBody List<DekDTO> requests
    ) {
        List<Dek> newDeks = dekService.storeDeks(dekMapper.toEntity(requests));
        return ResponseEntity.ok(dekMapper.toDTO(newDeks));
    }

    @GetMapping
    public ResponseEntity<Page<DekDTO>> getDeks(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute DekQuery dekQuery
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        Page<Dek> deks = dekService.getDeks(
                myId,
                dekQuery);
        return ResponseEntity.ok(dekMapper.toDTO(deks));
    }

    @GetMapping("/new")
    public ResponseEntity<Page<DekDTO>> getNewDeks(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute DekQuery dekQuery
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        Page<Dek> deks = dekService.getNewDeks(
                myId,
                dekQuery);
        return ResponseEntity.ok(dekMapper.toDTO(deks));
    }
}
