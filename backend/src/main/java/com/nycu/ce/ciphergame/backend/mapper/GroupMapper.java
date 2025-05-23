package com.nycu.ce.ciphergame.backend.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.group.CUGroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetGroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;

@Component
public class GroupMapper {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    public Group toEntity(CUGroupRequest dto) {
        return Group.builder()
                .name(dto.getName())
                .build();
    }

    public CUGroupResponse toDTOCreateUpdate(Group entity) {
        return CUGroupResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .members(entity.getMembers().stream()
                        .map(groupMemberMapper::toDTO)
                        .toList())
                .build();
    }

    public GetGroupResponse toDTOGet(Group entity) {
        return GetGroupResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .members(entity.getMembers().stream()
                        .map(groupMemberMapper::toDTO)
                        .toList())
                // .messages(entity.getMessages().stream()
                //         .map(messageMapper::toDTO)
                //         .toList())
                .build();
    }
}
