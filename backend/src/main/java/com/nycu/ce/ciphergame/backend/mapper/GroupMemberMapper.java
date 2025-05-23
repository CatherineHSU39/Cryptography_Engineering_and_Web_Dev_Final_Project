package com.nycu.ce.ciphergame.backend.mapper;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.group.GetAllGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GroupMemberResponse;
import com.nycu.ce.ciphergame.backend.entity.GroupMember;

@Component
public class GroupMemberMapper {

    public GroupMemberResponse toDTO(GroupMember member) {
        return GroupMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .build();
    }

    public GetAllGroupResponse toDTOGetAll(GroupMember entity) {
        return GetAllGroupResponse.builder()
                .id(entity.getGroup().getId())
                .name(entity.getGroup().getName())
                .build();
    }
}
