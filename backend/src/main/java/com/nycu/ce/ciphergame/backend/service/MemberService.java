package com.nycu.ce.ciphergame.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Member;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.MemberId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.MemberRepository;

@Service
public class MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    GroupService groupService;

    @Autowired
    UserService userService;

    public List<User> getAllUsersByGroupId(GroupId groupId) {
        return memberRepository.findAllUsers(groupId.toUUID());
    }

    public List<Group> getAllGroupsByUserId(UserId userId) {
        return memberRepository.findAllGroups(userId.toUUID());
    }

    public void addAllMembers(GroupId groupId, List<UserId> userIds) {
        Group group = groupService.getGroupById(groupId);
        List<User> users = userService.getAllUsersById(userIds);
        Set<Member> newMembers = users.stream()
                .map(user -> new Member(user, group))
                .collect(Collectors.toSet());

        this.addAllMembers(group.getMembers(), newMembers);
    }

    public void removeAllMembers(GroupId groupId, List<UserId> userIds) {
        Group group = groupService.getGroupById(groupId);
        Set<Member> targetMembers = this.getAllMembers(groupId, userIds);

        this.removeAllMembers(group.getMembers(), targetMembers);
    }

    public void addAllMembers(
            Set<Member> oldMembers,
            Set<Member> targetMembers
    ) {

        Set<Member> newMembers = targetMembers.stream()
                .filter(member -> !oldMembers.contains(member))
                .collect(Collectors.toSet());

        memberRepository.saveAll(newMembers);
    }

    public void removeAllMembers(
            Set<Member> oldMembers,
            Set<Member> targetMembers
    ) {
        Set<Member> removeMembers = targetMembers.stream()
                .filter(oldMembers::contains)
                .collect(Collectors.toSet());

        memberRepository.deleteAll(removeMembers);
    }

    public Member getMember(GroupId groupId, UserId userId) {
        Member member = memberRepository.findById(MemberId.builder()
                .userId(userId.toUUID())
                .groupId(groupId.toUUID())
                .build())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return member;
    }

    public Set<Member> getAllMembers(
            GroupId gourId,
            List<UserId> userIds
    ) {
        return userIds.stream()
                .map(userId -> this.getMember(gourId, userId))
                .collect(Collectors.toSet());
    }

    public void validateMember(GroupId groupId, UserId userId) {
        Boolean isValid = memberRepository.existsById(MemberId.builder()
                .userId(userId.toUUID())
                .groupId(groupId.toUUID())
                .build());
        if (!isValid) {
            throw new RuntimeException("Member invalid");
        }
    }

    public void validateAllMembers(List<GroupId> groupIds, UserId userId) {
        List<MemberId> memberIds = groupIds.stream()
                .map(groupId -> MemberId.builder()
                .userId(userId.toUUID())
                .groupId(groupId.toUUID())
                .build()
                ).collect(Collectors.toList());
        long count = memberRepository.countByIdIn(memberIds);
        Boolean isValid = count == memberIds.size();
        if (!isValid) {
            throw new RuntimeException("Member invalid");
        }
    }
}
