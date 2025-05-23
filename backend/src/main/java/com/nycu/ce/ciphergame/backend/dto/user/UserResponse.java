package com.nycu.ce.ciphergame.backend.dto.user;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String role;
}
