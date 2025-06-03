package com.nycu.ce.ciphergame.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.auth.dto.UserRegisterRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.entity.User;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;
import com.nycu.ce.ciphergame.auth.service.GAService;
import com.nycu.ce.ciphergame.auth.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    GAService gaService;

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        userService.createUser(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<UserSigninResponse> updateuser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserRegisterRequest request
    ) {
        UserSigninResponse response = userService.updateUser(
                jwt,
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/2fa")
    public ResponseEntity<String> setup2FA(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String secret = gaService.generateKey();
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.saveTotpSecret(userId, secret);
        String qrCode = gaService.generateQRUrl(secret, userId);
        return ResponseEntity.ok(qrCode);
    }
}
