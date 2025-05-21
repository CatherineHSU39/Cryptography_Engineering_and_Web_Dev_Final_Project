package com.nycu.ce.ciphergame.auth.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserSigninResponse {
    private UUID id;
    private String username;
    private String role;
    private String token;
}
