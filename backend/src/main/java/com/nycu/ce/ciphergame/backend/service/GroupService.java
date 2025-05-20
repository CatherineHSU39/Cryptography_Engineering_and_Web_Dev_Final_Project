package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.dto.group.CUGroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetAllGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetGroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.GroupMember;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.mapper.GroupMapper;
import com.nycu.ce.ciphergame.backend.repository.GroupMemberRepository;
import com.nycu.ce.ciphergame.backend.repository.GroupRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

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

        Group newGroup = groupRepository.save(group);

        Set<User> users = groupRequest.getMemberIds().stream()
            .map(userId -> new User(userId))
            .collect(Collectors.toSet());

        Set<GroupMember> newMembers = users.stream()
            .map(user -> new GroupMember(user, newGroup))
            .collect(Collectors.toSet());

        groupMemberRepository.saveAll(newMembers);
        return groupMapper.toDTOCreateUpdate(group);
    }

    public CUGroupResponse updateGroup(UUID groupId, CUGroupRequest groupRequest) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Set<UUID> existingUserIds = groupMemberRepository.findUserIdsByGroupId(groupId);

        Set<User> usersToAdd = groupRequest.getMemberIds().stream()
            .filter(userId -> !existingUserIds.contains(userId))
            .map(userId -> new User(userId))
            .collect(Collectors.toSet());

        Set<GroupMember> newMembers = usersToAdd.stream()
            .map(user -> new GroupMember(user, group))
            .collect(Collectors.toSet());

        groupMemberRepository.saveAll(newMembers);

        if (groupRequest.getName() != null) {
            group.setName(groupRequest.getName());
        }
        
        Group updatedGroup = groupRepository.save(group);
        return groupMapper.toDTOCreateUpdate(updatedGroup);
    }

    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }
}
