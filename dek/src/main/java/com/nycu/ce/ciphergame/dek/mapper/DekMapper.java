package com.nycu.ce.ciphergame.dek.mapper;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.dek.dto.DekDTO;
import com.nycu.ce.ciphergame.dek.entity.Dek;

@Component
public class DekMapper {

    public Dek toEntity(DekDTO dto) {
        return Dek.builder()
                .id(dto.getId())
                .ownerId(dto.getOwnerId())
                .nonce(Base64.getDecoder().decode(dto.getNonce())) // convert base64 string to byte[]
                .encrypted_dek(Base64.getDecoder().decode(dto.getEncrypted_dek())) // convert base64 string to byte[]
                .aad(dto.getAad()) // base64 string
                .build();
    }

    public List<Dek> toEntity(List<DekDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public DekDTO toDTO(Dek entity) {
        return DekDTO.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .encrypted_dek(Base64.getEncoder().encodeToString(entity.getEncrypted_dek())) // byte[] to base64 string
                .nonce(Base64.getEncoder().encodeToString(entity.getNonce())) // byte[] to base64 string
                .aad(entity.getAad())
                .build();
    }

    public List<DekDTO> toDTO(List<Dek> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<DekDTO> toDTO(Page<Dek> entities) {
        return entities.map(this::toDTO);
    }
}
