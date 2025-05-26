package com.nycu.ce.ciphergame.backend.service.me;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.entity.Member;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.repository.MemberRepository;
import com.nycu.ce.ciphergame.backend.service.GroupService;
import com.nycu.ce.ciphergame.backend.service.MemberService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MyMemberService {

    @Autowired
    GroupService groupService;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    public Set<Member> getAllMyMembers(
            UserId userId,
            GroupId groupId
    ) {
        memberService.validateMember(groupId, userId);
        return groupService.getGroupById(groupId).getMembers();
    }

    public void addAllMyMembers(
            UserId userId,
            GroupId groupId,
            List<UserId> newUserIds
    ) {
        memberService.validateMember(groupId, userId);
        memberService.addAllMembers(groupId, newUserIds);
    }

    public void removeAllMyMembers(
            UserId userId,
            GroupId groupId,
            List<UserId> removeUserIds
    ) {
        memberService.validateMember(groupId, userId);
        memberService.removeAllMembers(groupId, removeUserIds);
    }

    public void markMessageStatus(
            UserId userId,
            GroupId groupId,
            LocalDateTime timetemp
    ) {
        Member member = memberService.getMember(groupId, userId);
        member.setReadAt(timetemp);
        memberRepository.save(member);
    }
}
