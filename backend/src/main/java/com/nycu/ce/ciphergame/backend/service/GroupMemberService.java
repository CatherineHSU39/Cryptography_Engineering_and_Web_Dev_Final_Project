package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.GroupMember;
import com.nycu.ce.ciphergame.backend.entity.User;

@Service
public class GroupMemberService {

    public Set<GroupMember> getAdditionalMember(Set<GroupMember> newList, Set<GroupMember> baseList) {
        return newList.stream()
                .filter(member -> !baseList.contains(member))
                .collect(Collectors.toSet());
    }

    public void createAllMemberByUser(Group group, List<User> users) {
        group.getMembers().addAll(users.stream()
                .map(user -> new GroupMember(user, group))
                .toList()
        );
    }

}
