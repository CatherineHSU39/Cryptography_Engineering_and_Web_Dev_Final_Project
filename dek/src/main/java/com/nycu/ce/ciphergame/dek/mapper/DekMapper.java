package com.nycu.ce.ciphergame.dek.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.dek.dto.DekRequest;
import com.nycu.ce.ciphergame.dek.dto.DekResponse;
import com.nycu.ce.ciphergame.dek.entity.Dek;

@Component
public class DekMapper {

    public Dek toEntity(DekRequest dto) {
        return Dek.builder()
                .ownerId(dto.getOwnerId())
                .dek(dto.getDek())
                .build();
    }

    public List<Dek> toEntity(List<DekRequest> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public DekResponse toDTO(Dek entity) {
        return DekResponse.builder()
                .id(entity.getId())
                .dek(entity.getDek())
                .build();
    }

    public List<DekResponse> toDTO(List<Dek> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
