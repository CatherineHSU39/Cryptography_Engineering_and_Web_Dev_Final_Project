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
import com.nycu.ce.ciphergame.backend.entity.GroupMemberId;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.mapper.GroupMapper;
import com.nycu.ce.ciphergame.backend.mapper.GroupMemberMapper;
import com.nycu.ce.ciphergame.backend.repository.GroupMemberRepository;
import com.nycu.ce.ciphergame.backend.repository.GroupRepository;
import com.nycu.ce.ciphergame.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    public GetGroupResponse getGroupById(UUID userId, UUID groupId) {

        Group group = groupRepository.findById(groupId)
                .orElse(null);

        return groupMapper.toDTOGet(group);
    }

    @Transactional
    public List<GetAllGroupResponse> getAllGroups(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getGroup().stream()
                .map(groupMemberMapper::toDTOGetAll)
                .collect(Collectors.toList());
    }

    @Transactional
    public CUGroupResponse createGroup(UUID creatorId, CUGroupRequest groupRequest) {
        // Step 1: Save group
        Group group = Group.builder()
                .name(groupRequest.getName())
                .build();
        Group newGroup = groupRepository.save(group); // Group now has ID

        // Step 2: Add members to group
        groupRequest.getMemberIds().add(creatorId);
        List<User> users = userRepository.findAllById(groupRequest.getMemberIds());
        if (users.size() != groupRequest.getMemberIds().size()) {
            Set<UUID> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            List<UUID> missing = groupRequest.getMemberIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new RuntimeException("Some users not found: " + missing);
        }
        newGroup.getMembers().addAll(users.stream()
                .map(user -> new GroupMember(user, newGroup))
                .toList()
        );
        return groupMapper.toDTOCreateUpdate(newGroup);
    }

    public CUGroupResponse updateGroup(UUID groupId, CUGroupRequest groupRequest) {
        Group group = groupRepository.findById(groupId)
                .orElse(null);

        List<UUID> userIds = groupRequest.getMemberIds().stream().distinct().toList();
        List<User> users = userRepository.findAllById(userIds);
        List<GroupMember> targetMembers = users.stream()
                .map(user -> new GroupMember(user, group))
                .toList();

        if (groupRequest.getName() != null) {
            group.setName(groupRequest.getName());
        }

        List<GroupMember> oldMembers = group.getMembers();
        List<GroupMember> newMembers = targetMembers.stream()
                .filter(member -> !oldMembers.contains(member))
                .toList();
        List<GroupMember> removeMembers = oldMembers.stream()
                .filter(member -> !newMembers.contains(member))
                .toList();
        group.getMembers().addAll(newMembers);
        group.getMembers().removeAll(removeMembers);

        Group updatedGroup = groupRepository.save(group); // cascades GroupMember
        return groupMapper.toDTOCreateUpdate(updatedGroup);
    }

    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }

    public boolean isUserInGroup(UUID userId, UUID groupId) {
        return groupMemberRepository.existsById(GroupMemberId.builder()
                .userId(userId)
                .groupId(groupId)
                .build());
    }
}
