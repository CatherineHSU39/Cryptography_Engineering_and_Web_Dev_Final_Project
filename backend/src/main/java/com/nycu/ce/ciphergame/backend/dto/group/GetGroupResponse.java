package com.nycu.ce.ciphergame.backend.dto.group;

import java.util.List;
import java.util.UUID;

import com.nycu.ce.ciphergame.backend.dto.message.MessageResponseDTO;

import lombok.Data;

@Data
public class GetGroupResponse {
    private UUID id;
    private String name;
    private List<GroupMemberResponse> members;
    private List<MessageResponseDTO> messages;
}
