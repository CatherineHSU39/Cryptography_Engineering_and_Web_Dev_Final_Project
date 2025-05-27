package com.nycu.ce.ciphergame.backend.controller.me;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.group.GroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.GroupResponse;
import com.nycu.ce.ciphergame.backend.dto.member.MemberRequest;
import com.nycu.ce.ciphergame.backend.dto.member.MemberResponse;
import com.nycu.ce.ciphergame.backend.dto.message.MessageQuery;
import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
import com.nycu.ce.ciphergame.backend.dto.message.MessageStatusRequest;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.Member;
import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
import com.nycu.ce.ciphergame.backend.mapper.GroupMapper;
import com.nycu.ce.ciphergame.backend.mapper.MemberMapper;
import com.nycu.ce.ciphergame.backend.mapper.MessageMapper;
import com.nycu.ce.ciphergame.backend.service.MemberService;
import com.nycu.ce.ciphergame.backend.service.me.MyGroupService;
import com.nycu.ce.ciphergame.backend.service.me.MyMemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/me/groups")
public class MyGroupController {

    @Autowired
    private MyGroupService myGroupService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MyMemberService myMemberService;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MessageMapper messageMapper;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GroupRequest request
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        Group newGroup = myGroupService.createMyGroup(userId, request.getName());
        return ResponseEntity.ok(groupMapper.toDTO(newGroup));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        List<Group> groups = myGroupService.getAllMyGroups(userId);
        return ResponseEntity.ok(groupMapper.toDTO(groups));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId,
            @Valid @RequestBody GroupRequest request
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        Group updatedGroup = myGroupService.updateMyGroup(
                userId,
                groupId,
                request.getName()
        );
        return ResponseEntity.ok(groupMapper.toDTO(updatedGroup));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId
    ) {
        UserId userId = UserId.fromString(jwt.getSubject());
        myGroupService.deleteMyGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/members/join")
    public ResponseEntity<Void> joinGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        memberService.addAllMembers(groupId, List.of(myId));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<Set<MemberResponse>> getUsersInGroup(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @PathVariable GroupId groupId
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        Set<Member> members = myMemberService.getAllMyMembers(
                myId,
                groupId
        );
        return ResponseEntity.ok(memberMapper.toDTO(members));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addUsersToGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId,
            @Valid @RequestBody MemberRequest memberRequest
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        myMemberService.addAllMyMembers(
                myId,
                groupId,
                memberRequest.getMemberIds().stream()
                        .map(UserId::fromUUID)
                        .collect(Collectors.toList())
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{groupId}/members")
    public ResponseEntity<Void> removeUsersFromGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId,
            @Valid @RequestBody MemberRequest memberRequest
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        myMemberService.removeAllMyMembers(
                myId,
                groupId,
                memberRequest.getMemberIds().stream()
                        .map(UserId::fromUUID)
                        .collect(Collectors.toList())
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessagesInGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId,
            @Valid @ModelAttribute MessageQuery messageQuery
    ) {
        UserId myId = UserId.fromString(jwt.getSubject());
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Page<Message> messages = myGroupService.getAllMessagesInGroup(
                myId,
                groupId,
                messageQuery.toPageable(sort)
        );
        return ResponseEntity.ok(messageMapper.toDTO(messages));
    }

    @PutMapping("/{groupId}/messages/status")
    public ResponseEntity<Void> markMessageStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable GroupId groupId,
            @Valid @RequestBody MessageStatusRequest messageStatusRequest
    ) {
        UserId senderId = UserId.fromString(jwt.getSubject());
        myMemberService.markMessageStatus(
                senderId,
                groupId,
                messageStatusRequest.getTimestemp()
        );

        return ResponseEntity.ok().build();
    }
}
