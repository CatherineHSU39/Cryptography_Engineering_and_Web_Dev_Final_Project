package com.nycu.ce.ciphergame.dek.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DekQuery {

    @Size(max = 100, message = "At most 100 dekIds can be queried at a time")
    @NotNull(message = "At least one dekId must be provided")
    List<UUID> dekIds = new ArrayList<>();
}
