package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nycu.ce.ciphergame.backend.repository.UserRepository;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(UserId userId) {
        User user = userRepository.findById(userId.toUUID())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsersById(List<UserId> userIds) {
        List<UUID> ids = userIds.stream().map(id -> id.toUUID()).collect(Collectors.toList());
        return userRepository.findAllById(ids);
    }

    public User updateUser(UserId userId, String username) {
        User user = userRepository.findById(userId.toUUID())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(username);

        userRepository.save(user);
        return user;
    }
}
