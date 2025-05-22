package com.nycu.ce.ciphergame.auth.dto;

import lombok.Data;

@Data
public class UserSigninRequest {

    private String username;
    private String password;
}
