package com.nycu.ce.ciphergame.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.auth.dto.UserRegisterRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.mapper.UserMapper;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;
import com.nycu.ce.ciphergame.auth.security.JwtUtil;
import com.nycu.ce.ciphergame.auth.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService userService;
    @Autowired
    UserMapper userMapper;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtil jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<UserSigninResponse> authenticateUser(@RequestBody UserSigninRequest user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateUserToken(userDetails);
        UserSigninResponse response = userMapper.toDto(userDetails, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody TwoFaRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (twoFactorAuthService.verifyCode(user.getTwoFaSecret(), request.getCode())) {
            // Issue JWT here
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
        }
    }
}
