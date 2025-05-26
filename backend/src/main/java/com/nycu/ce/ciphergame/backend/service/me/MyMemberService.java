package com.nycu.ce.ciphergame.backend.service.me;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Member;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.service.GroupService;
import com.nycu.ce.ciphergame.backend.service.MemberService;
import com.nycu.ce.ciphergame.backend.service.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MyMemberService {

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    MemberService memberService;

    public Set<Member> getAllMyMembers(UserId userId, GroupId gourId) {
        memberService.validateMember(gourId, userId);
        return groupService.getGroupById(gourId).getMembers();
    }

    public void addAllMyMembers(UserId userId, GroupId groupId, List<UserId> userIds) {
        Member member = memberService.getMember(groupId, userId);
        List<User> users = userService.getAllUsersById(userIds);
        memberService.addAllMembers(member.getGroup(), users);
    }

    public void removeAllMyMembers(UserId userId, GroupId groupId, List<UserId> userIds) {
        Member member = memberService.getMember(groupId, userId);
        List<User> users = userService.getAllUsersById(userIds);
        memberService.removeAllMembers(member.getGroup(), users);
    }
}
