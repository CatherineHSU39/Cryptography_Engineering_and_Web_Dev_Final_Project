package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.mapper.UserMapper;
import com.nycu.ce.ciphergame.backend.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return userMapper.toDTO(users);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UserId id) {
        User user = userService.getUserById(id);
        return userMapper.toDTO(user);
    }
}
