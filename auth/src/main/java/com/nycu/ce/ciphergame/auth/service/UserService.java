package com.nycu.ce.ciphergame.auth.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.auth.dto.UserRegisterRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.entity.User;
import com.nycu.ce.ciphergame.auth.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public UserSigninResponse createUser(UserRegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);

        UserSigninResponse response = new UserSigninResponse();
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void saveTotpSecret(UUID id, String secret) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTotpSecret(secret);
        userRepository.save(user);
    }
}
