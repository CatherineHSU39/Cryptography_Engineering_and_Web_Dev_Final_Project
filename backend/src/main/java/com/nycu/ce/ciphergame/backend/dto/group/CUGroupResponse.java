package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CUGroupResponse {

    private UUID id;
    private String name;
    private List<GroupMemberResponse> members;
}
