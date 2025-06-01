package com.nycu.ce.ciphergame.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dto.group.GroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.GroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;

@Component
public class GroupMapper {

    public Group toEntity(GroupRequest dto) {
        return new Group(dto.getName());
    }

    public GroupResponse toDTO(Group entity) {
        return GroupResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public List<GroupResponse> toDTO(List<Group> entity) {
        return entity.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
