package com.nycu.ce.ciphergame.dek.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DekResponse {

    private UUID id;
    private String dek;
}
