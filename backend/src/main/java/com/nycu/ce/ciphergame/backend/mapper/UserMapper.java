package com.nycu.ce.ciphergame.backend.mapper;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.user.UserRequest;
import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.entity.User;

@Component
public class UserMapper {

    public UserResponse toDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }

    public User toEntity(UserRequest userRequestDTO) {
        return User.builder()
                .username(userRequestDTO.getUsername())
                .build();
    }
}
