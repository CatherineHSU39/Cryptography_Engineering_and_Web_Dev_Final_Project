package com.nycu.ce.ciphergame.backend.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must be at most 100 characters")
    private String name;
}
