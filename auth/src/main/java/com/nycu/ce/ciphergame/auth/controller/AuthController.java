package com.nycu.ce.ciphergame.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.auth.dto.TwoFaRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninRequest;
import com.nycu.ce.ciphergame.auth.dto.UserSigninResponse;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;
import com.nycu.ce.ciphergame.auth.service.AuthService;
import com.nycu.ce.ciphergame.auth.service.CustomUserDetailsService;
import com.nycu.ce.ciphergame.auth.service.UserService;

@RestController
@RequestMapping("/identity")
public class AuthController {

    @Autowired
    UserService userService;
    @Autowired
    CustomUserDetailsService userDetailsService;
    @Autowired
    AuthService authService;
    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/token")
    public ResponseEntity<?> authenticateUser(@RequestBody UserSigninRequest user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserSigninResponse response = authService.signUserJwt(userDetails, false, true);
        return ResponseEntity.status(206).body(response);
    }

    @PostMapping("/2fa-verification")
    public ResponseEntity<?> verify2FA(@AuthenticationPrincipal Jwt jwt, @RequestBody TwoFaRequest req) {
        if ((boolean) jwt.getClaim("2fa_verified")) {
            return ResponseEntity.status(400).body("2FA already verified");
        }

        UUID userId = UUID.fromString(jwt.getSubject()); // from initial login
        Boolean is2faVerified = authService.verify2fa(userId, req.getCode());
        if (!is2faVerified) {
            return ResponseEntity.status(401).body("Invalid 2FA code");
        }

        CustomUserDetails userDetails = userDetailsService.loadUserById(userId);
        UserSigninResponse response = authService.signUserJwt(userDetails, true, false);
        return ResponseEntity.ok(response);
    }
}
