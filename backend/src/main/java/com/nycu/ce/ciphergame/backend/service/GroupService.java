package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.dto.group.CUGroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetAllGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetGroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.mapper.GroupMapper;
import com.nycu.ce.ciphergame.backend.repository.GroupRepository;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMapper groupMapper;

    public GetGroupResponse getGroupById(UUID id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDTOGet)
                .orElse(null);
    }

    public List<GetAllGroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(groupMapper::toDTOGetAll)
                .collect(Collectors.toList());
    }
    
    public CUGroupResponse createGroup(CUGroupRequest groupRequest) {
        Group group = groupMapper.toEntity(groupRequest);
        group = groupRepository.save(group);
        return groupMapper.toDTOCreateUpdate(group);
    }

    public CUGroupResponse updateGroup(UUID id, CUGroupRequest groupRequestDTO) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        groupMapper.updateEntityFromDTO(groupRequestDTO, group);
        group = groupRepository.save(group);
        return groupMapper.toDTOCreateUpdate(group);
    }

    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }
}
