package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResponse {

    private UUID id;
    private String name;
}
