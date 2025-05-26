package com.nycu.ce.ciphergame.backend.dto.message;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageUpdateRequest {
    
    @NotBlank(message = "Message content must not be empty")
    @Size(max = 5000, message = "Message content too long")
    private String content;
}
