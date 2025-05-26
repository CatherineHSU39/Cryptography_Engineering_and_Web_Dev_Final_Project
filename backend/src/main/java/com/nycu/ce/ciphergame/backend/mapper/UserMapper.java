package com.nycu.ce.ciphergame.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<UserResponse> toDTO(List<User> user) {
        return user.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public User toEntity(UserRequest userRequestDTO) {
        return new User(userRequestDTO.getUsername());
    }
}
