package com.nycu.ce.ciphergame.backend.dto.recipient;

import java.util.List;

import com.nycu.ce.ciphergame.backend.entity.id.GroupId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipientRequest {

    @NotEmpty(message = "At least one groupId must be provided")
    List<GroupId> groupIds;
}
