package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.Set;
import java.util.UUID;

import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;

import lombok.Data;

@Data
public class GetGroupResponse {
    private UUID id;
    private String name;
    private Set<GroupMemberResponse> members;
    private Set<MessageResponse> messages;
}
