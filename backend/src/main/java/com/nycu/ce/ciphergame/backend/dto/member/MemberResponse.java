package com.nycu.ce.ciphergame.backend.dto.member;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {

    private UUID userId;
    private String username;
}
