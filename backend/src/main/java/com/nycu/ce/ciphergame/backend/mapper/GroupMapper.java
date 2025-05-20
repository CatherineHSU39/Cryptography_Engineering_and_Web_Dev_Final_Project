package com.nycu.ce.ciphergame.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.nycu.ce.ciphergame.backend.dto.group.CUGroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetAllGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetGroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    Group toEntity(CUGroupRequest dto);

    CUGroupResponse toDTOCreateUpdate(Group entity);

    GetGroupResponse toDTOGet(Group entity);

    GetAllGroupResponse toDTOGetAll(Group entity);

    void updateEntityFromDTO(CUGroupRequest dto, @MappingTarget Group entity);
}


