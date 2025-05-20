package com.nycu.ce.ciphergame.backend.dto.user;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String role;
}
