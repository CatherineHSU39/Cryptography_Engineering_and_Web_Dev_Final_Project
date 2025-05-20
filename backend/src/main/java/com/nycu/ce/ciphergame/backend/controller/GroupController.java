package com.nycu.ce.ciphergame.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CUGroupResponse> createGroup(@RequestBody CUGroupRequest groupRequest) {
        CUGroupResponse group = groupService.createGroup(groupRequest);
        return ResponseEntity.ok(group);
    }

    @GetMapping()
    public ResponseEntity<List<GetAllGroupResponse>> getAllGroups() {
        List<GetAllGroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetGroupResponse> getGroupById(@PathVariable UUID id) {
        GetGroupResponse group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CUGroupResponse> updateGroup(@PathVariable UUID id, @RequestBody CUGroupRequest group) {
        CUGroupResponse updatedGroup = groupService.updateGroup(id, group);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
