package com.nycu.ce.ciphergame.backend.dto.user;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String role;
}
