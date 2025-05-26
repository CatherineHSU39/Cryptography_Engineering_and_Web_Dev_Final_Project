package com.nycu.ce.ciphergame.backend.service.me;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.service.GroupService;
import com.nycu.ce.ciphergame.backend.service.MemberService;
import com.nycu.ce.ciphergame.backend.service.MessageService;
import com.nycu.ce.ciphergame.backend.service.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MyGroupService {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private MessageService messageService;

    public Group createMyGroup(UserId userId, String name) {
        User user = userService.getUserById(userId);
        // Step 1: Save group
        Group group = groupService.createGroup(name);

        // Step 2: Add members to group
        memberService.addAllMembers(group, List.of(user));
        return group;
    }

    public List<Group> getAllMyGroups(UserId userId) {
        return memberService.getAllGroupsByUserId(userId);
    }

    public Group updateMyGroup(UserId userId, GroupId groupId, String name) {
        memberService.validateMember(groupId, userId);

        return groupService.updateGroup(groupId, name);
    }

    public void deleteMyGroup(UserId userId, GroupId groupId) {
        memberService.validateMember(groupId, userId);

        groupService.deleteGroup(groupId);
    }

    public Page<Message> getAllMessagesInGroup(
            UserId userId,
            GroupId groupId,
            Pageable pageable
    ) {
        memberService.validateMember(groupId, userId);
        return messageService.getMessagesByGroupId(groupId, pageable);
    }
}
