package com.nycu.ce.ciphergame.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSigninRequest {

    @NotBlank(message = "username is required")
    @Size(max = 100, message = "username must be at most 100 characters")
    private String username;
    @NotBlank(message = "password is required")
    @Size(max = 100, message = "password must be at most 100 characters")
    private String password;
}
