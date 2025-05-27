package com.nycu.ce.ciphergame.backend.controller.me;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.user.UserRequest;
import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.mapper.UserMapper;
import com.nycu.ce.ciphergame.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/me")
public class MeController {

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @GetMapping
    public ResponseEntity<UserResponse> getMyUserInfo(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId id = UserId.fromString(jwt.getSubject());
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateMyUserInfo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserRequest request
    ) {
        UserId id = UserId.fromString(jwt.getSubject());
        User user = userService.updateUser(id, request.getUsername());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }
}
