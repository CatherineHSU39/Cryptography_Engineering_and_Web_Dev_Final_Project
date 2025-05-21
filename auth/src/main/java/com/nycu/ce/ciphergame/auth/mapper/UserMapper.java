package com.nycu.ce.ciphergame.auth.mapper;

import org.mapstruct.Mapper;

import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.dto.UserRegisterRequest;
import com.nycu.ce.ciphergame.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegisterRequest dto);
    UserSigninResponse toDto(User entity);
}
