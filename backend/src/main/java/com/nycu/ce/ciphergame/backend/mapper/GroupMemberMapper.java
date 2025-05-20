package com.nycu.ce.ciphergame.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nycu.ce.ciphergame.backend.dto.group.GroupMemberResponse;
import com.nycu.ce.ciphergame.backend.entity.GroupMember;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    GroupMemberResponse toDTO(GroupMember member);

    List<GroupMemberResponse> toDTOList(List<GroupMember> members);
}
