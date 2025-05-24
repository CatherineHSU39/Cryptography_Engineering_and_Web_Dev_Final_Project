package com.nycu.ce.ciphergame.backend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.dto.user.UserRequest;
import com.nycu.ce.ciphergame.backend.service.UserService;


@RestController
@RequestMapping("/app/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRequest user) {
        UUID id = UUID.fromString(jwt.getSubject());
        return userService.updateUser(id, user);
    }
}
