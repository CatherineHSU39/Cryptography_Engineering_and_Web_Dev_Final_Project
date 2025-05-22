package com.nycu.ce.ciphergame.auth.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.entity.User;
import com.nycu.ce.ciphergame.auth.mapper.UserMapper;
import com.nycu.ce.ciphergame.auth.repository.UserRepository;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;
import com.nycu.ce.ciphergame.auth.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    JwtUtil jwtUtils;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GAService gaService;

    public UserSigninResponse signUserJwt(CustomUserDetails userDetails, Boolean is2faVarified, Boolean requires2Fa) {
        String token = jwtUtils.generateUserToken(userDetails, is2faVarified);
        UserSigninResponse response = userMapper.toDto(userDetails, token, requires2Fa);
        return response;
    }

    public String signServiceJwt(String serviceName) {
        return jwtUtils.generateServiceToken(serviceName);
    }

    public Boolean verify2fa(UUID userId, int code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return gaService.isValid(user.getTotpSecret(), code);
    }
}
