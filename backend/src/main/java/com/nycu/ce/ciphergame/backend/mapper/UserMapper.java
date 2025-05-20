package com.nycu.ce.ciphergame.backend.mapper;

import org.mapstruct.Mapper;

import com.nycu.ce.ciphergame.backend.dto.user.UserRequestDTO;
import com.nycu.ce.ciphergame.backend.dto.user.UserResponseDTO;
import com.nycu.ce.ciphergame.backend.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toDTO(User user);
    User toEntity(UserRequestDTO userRequestDTO);
}
