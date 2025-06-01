package com.nycu.ce.ciphergame.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.repository.GroupRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public Group getGroupById(GroupId groupId) {

        Group group = groupRepository.findById(groupId.toUUID())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return group;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group createGroup(String name) {
        Group group = new Group(name);
        groupRepository.save(group); // Group now has ID
        return group;
    }

    public Group updateGroup(GroupId groupId, String name) {
        Group group = groupRepository.findById(groupId.toUUID())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setName(name);

        groupRepository.save(group);
        return group;
    }

    public void deleteGroup(GroupId id) {
        groupRepository.deleteById(id.toUUID());
    }
}
