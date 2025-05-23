package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class CUGroupRequest {

    private String name;
    private Set<UUID> memberIds;
}
