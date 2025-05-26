package com.nycu.ce.ciphergame.backend.dto.member;

import java.util.List;

import com.nycu.ce.ciphergame.backend.entity.id.UserId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberRequest {

    @NotEmpty(message = "At least one member must be provided")
    private List<@Valid UserId> memberIds;
}
