package com.nycu.ce.ciphergame.backend.dto.message;

import java.util.List;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageCreateRequest {

    @NotBlank(message = "Message content must not be empty")
    @Size(max = 5000, message = "Message content too long")
    private String content;

    @NotEmpty(message = "At least one groupId must be provided")
    private List<UUID> groupIds;
}
