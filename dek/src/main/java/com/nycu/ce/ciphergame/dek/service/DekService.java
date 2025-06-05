package com.nycu.ce.ciphergame.dek.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.dek.dto.DekQuery;
import com.nycu.ce.ciphergame.dek.entity.Dek;
import com.nycu.ce.ciphergame.dek.entity.User;
import com.nycu.ce.ciphergame.dek.entity.id.DekId;
import com.nycu.ce.ciphergame.dek.entity.id.UserId;
import com.nycu.ce.ciphergame.dek.repository.DekRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DekService {

    @Autowired
    private DekRepository dekRepository;

    @Autowired
    private UserService userService;

    public Page<Dek> getDeks(UserId userId, DekQuery dekQuery) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = dekQuery.toPageable(sort);
        return dekRepository.findAllByOwnerId(userId.toUUID(), pageable);
    }

    public Page<Dek> getNewDeks(UserId userId, DekQuery dekQuery) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = dekQuery.toPageable(sort);

        User user = userService.getUserById(userId);
        LocalDateTime fetchNewAt = user.getFetchNewAt();
        Page<Dek> page = dekRepository.findAllByOwnerIdSinceCreatedAt(
                fetchNewAt,
                userId.toUUID(),
                pageable
        );

        page.getContent().stream().findFirst()
                .map(Dek::getCreatedAt)
                .ifPresent(user::setFetchNewAt);

        return page;
    }

    public Dek getDekById(UserId userId, DekId dekId) {
        return dekRepository.findByOwnerIdAndId(userId.toUUID(), dekId.toUUID());
    }

    public List<Dek> storeDeks(List<Dek> newDeks) {

        dekRepository.saveAll(newDeks);
        return newDeks;
    }
}
