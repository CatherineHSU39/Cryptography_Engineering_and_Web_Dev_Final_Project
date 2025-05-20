package com.nycu.ce.ciphergame.backend.mapper;

import org.mapstruct.Mapper;

import com.nycu.ce.ciphergame.backend.dto.user.UserRequest;
import com.nycu.ce.ciphergame.backend.dto.user.UserResponse;
import com.nycu.ce.ciphergame.backend.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDTO(User user);
    
    User toEntity(UserRequest userRequestDTO);
}
