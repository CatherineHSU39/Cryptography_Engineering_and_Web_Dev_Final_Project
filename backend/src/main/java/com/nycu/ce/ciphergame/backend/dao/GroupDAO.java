package com.nycu.ce.ciphergame.backend.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Group;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class GroupDAO {

    @PersistenceContext
    private EntityManager em;

    public Group getDetachedGroupById(UUID id) {
        Group group = em.find(Group.class, id);
        em.detach(group);
        return group;
    }
}
