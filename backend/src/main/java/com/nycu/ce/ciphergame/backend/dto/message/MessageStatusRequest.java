package com.nycu.ce.ciphergame.backend.dto.message;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageStatusRequest {

    @NotEmpty(message = "timestemp of last read must be provided")
    private LocalDateTime timestemp;
}
