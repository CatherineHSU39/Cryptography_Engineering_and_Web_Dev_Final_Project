package com.nycu.ce.ciphergame.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nycu.ce.ciphergame.auth.dto.UserRegisterRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.entity.User;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totpSecret", ignore = true)
    User toEntity(UserRegisterRequest dto);

    UserSigninResponse toDto(CustomUserDetails userDetails, String token, Boolean requires2fa);
}
