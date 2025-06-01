package com.nycu.ce.ciphergame.backend.controller.group;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nycu.ce.ciphergame.backend.dto.group.GroupRequest;
import com.nycu.ce.ciphergame.backend.dto.group.GroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.mapper.GroupMapper;
import com.nycu.ce.ciphergame.backend.service.GroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMapper groupMapper;

    @GetMapping()
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groupMapper.toDTO(groups));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable GroupId groupId
    ) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(groupMapper.toDTO(group));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable GroupId groupId,
            @Valid @RequestBody GroupRequest request
    ) {
        Group updatedGroup = groupService.updateGroup(groupId, request.getName());
        return ResponseEntity.ok(groupMapper.toDTO(updatedGroup));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable GroupId groupId
    ) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }
}
