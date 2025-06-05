package com.nycu.ce.ciphergame.dek.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.dek.entity.User;
import com.nycu.ce.ciphergame.dek.entity.id.UserId;
import com.nycu.ce.ciphergame.dek.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User getOrCreateUserById(UserId userId) {
        User user = userRepository.findById(userId.toUUID())
                .orElseGet(() -> new User(userId.toUUID()));

        return user;
    }

    public User getUserById(UserId userId) {
        return userRepository.findById(userId.toUUID())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Void updateUser(User user) {
        userRepository.save(user);
        return null;
    }

    public Void syncUser(
            Jwt jwt
    ) {
        User user = getOrCreateUserById(
                UserId.fromString(jwt.getSubject())
        );

        user.setFetchNewAt(LocalDateTime.now());

        userRepository.save(user);
        return null;
    }
}
