package com.nycu.ce.ciphergame.backend.dto.message;

import java.util.List;

import com.nycu.ce.ciphergame.backend.entity.id.GroupId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageCreateRequest {

    @NotBlank(message = "Message content must not be empty")
    @Size(max = 5000, message = "Message content too long")
    private String content;

    @NotEmpty(message = "At least one groupId must be provided")
    private List<GroupId> groupIds;
}
