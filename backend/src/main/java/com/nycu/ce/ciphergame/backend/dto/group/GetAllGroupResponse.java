package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.UUID;

import lombok.Data;

@Data
public class GetAllGroupResponse {
    private UUID id;
    private String name;
}
