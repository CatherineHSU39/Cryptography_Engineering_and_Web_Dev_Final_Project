package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private UUID userId;
    private String username;
}
