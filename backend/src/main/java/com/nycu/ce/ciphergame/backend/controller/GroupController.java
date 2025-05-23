package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.group.CUGroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetAllGroupResponse;
import com.nycu.ce.ciphergame.backend.dto.group.GetGroupResponse;
import com.nycu.ce.ciphergame.backend.service.GroupService;

@RestController
@RequestMapping("/app/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping()
    public ResponseEntity<CUGroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt, @RequestBody CUGroupRequest group) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CUGroupResponse newGroup = groupService.createGroup(userId, group);
        return ResponseEntity.ok(newGroup);
    }

    @GetMapping()
    public ResponseEntity<List<GetAllGroupResponse>> getAllGroups(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<GetAllGroupResponse> groups = groupService.getAllGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GetGroupResponse> getGroupById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(userId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        GetGroupResponse group = groupService.getGroupById(userId, groupId);
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<CUGroupResponse> updateGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @RequestBody CUGroupRequest group) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(userId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        CUGroupResponse updatedGroup = groupService.updateGroup(groupId, group);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (!groupService.isUserInGroup(userId, groupId)) {
            return ResponseEntity.status(403).build();
        }
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
