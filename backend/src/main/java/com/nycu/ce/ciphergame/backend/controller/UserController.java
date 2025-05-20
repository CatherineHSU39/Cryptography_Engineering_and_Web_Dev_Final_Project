package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.user.UserResponseDTO;
import com.nycu.ce.ciphergame.backend.service.UserService;

@RestController
@RequestMapping("/app/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    // @GetMapping("/me")
    // public UserResponseDTO getCurrentUser() {
    //     return userService.getCurrentUser();
    // }

    // @PutMapping("/me")
    // public UserResponseDTO updateCurrentUser(@RequestBody UserResponseDTO user) {
    //     return userService.updateCurrentUser(user);
    // }
}
