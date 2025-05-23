package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.List;
import java.util.UUID;

import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetGroupResponse {

    private UUID id;
    private String name;
    private List<GroupMemberResponse> members;
    private List<MessageResponse> messages;
}
