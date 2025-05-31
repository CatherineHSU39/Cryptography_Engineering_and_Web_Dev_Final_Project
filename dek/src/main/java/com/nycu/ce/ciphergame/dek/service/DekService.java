package com.nycu.ce.ciphergame.dek.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.dek.entity.Dek;
import com.nycu.ce.ciphergame.dek.entity.id.DekId;
import com.nycu.ce.ciphergame.dek.entity.id.UserId;
import com.nycu.ce.ciphergame.dek.repository.DekRepository;

@Service
public class DekService {

    @Autowired
    private DekRepository dekRepository;

    public List<Dek> getDeks(UserId userId, List<DekId> dekIds) {
        List<UUID> ids = dekIds.stream()
                .map(DekId::toUUID)
                .collect(Collectors.toList());
        return dekRepository.findAllOwnerIdAndIdIn(userId.toUUID(), ids);
    }

    public List<Dek> storeDeks(List<Dek> newDeks) {

        dekRepository.saveAll(newDeks);
        return newDeks;
    }
}
