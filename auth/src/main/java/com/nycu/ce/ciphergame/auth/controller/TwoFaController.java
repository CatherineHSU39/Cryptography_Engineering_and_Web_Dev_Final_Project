package com.nycu.ce.ciphergame.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.auth.service.GAService;
import com.nycu.ce.ciphergame.auth.service.UserService;

@RestController
@RequestMapping("/2fa")
public class TwoFaController {

    @Autowired
    GAService gaService;

    @Autowired
    UserService userService;

    @PostMapping("/setup")
    public ResponseEntity<String> setup2FA(@AuthenticationPrincipal Jwt jwt) {
        String secret = gaService.generateKey();
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.saveTotpSecret(userId, secret);
        String qrCode = gaService.generateQRUrl(secret, userId);
        return ResponseEntity.ok(qrCode);
    }
}
