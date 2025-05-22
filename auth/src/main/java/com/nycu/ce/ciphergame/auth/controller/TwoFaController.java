package com.nycu.ce.ciphergame.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;
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
    public ResponseEntity<String> setup2FA(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String secret = gaService.generateKey();
        userService.saveTotpSecret(userDetails.getId(), secret);
        String qrCode = gaService.generateQRUrl(userDetails.getUsername(), secret);
        return ResponseEntity.ok(qrCode);
    }
}
