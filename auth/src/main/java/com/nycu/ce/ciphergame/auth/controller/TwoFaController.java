package com.nycu.ce.ciphergame.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;

import com.nycu.ce.ciphergame.auth.service.GAService;
import com.nycu.ce.ciphergame.auth.service.UserService;

public class TwoFaController {

    @Autowired
    GAService gaService;

    @Autowired
    UserService userService;

    @PostMapping("/2fa/setup")
    public ResponseEntity<String> setup2FA(@AuthenticationPrincipal UserDetails userDetails) {
        String secret = gaService.generateKey();
        userService.saveTotpSecret(userDetails.getUsername(), secret);
        String qrCode = gaService.generateQRUrl(userDetails.getUsername(), secret);
        return ResponseEntity.ok(qrCode);
    }
}
