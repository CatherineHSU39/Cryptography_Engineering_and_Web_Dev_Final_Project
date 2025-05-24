package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.dto.user.UserRequest;
import com.nycu.ce.ciphergame.backend.mapper.UserMapper;
import com.nycu.ce.ciphergame.backend.repository.UserRepository;

import com.nycu.ce.ciphergame.backend.entity.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElse(null);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(UUID id, UserRequest dto) {
        User user = userRepository.findById(id).orElse(null);

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }

        userRepository.save(user);
        return userMapper.toDTO(user);
    }
}
