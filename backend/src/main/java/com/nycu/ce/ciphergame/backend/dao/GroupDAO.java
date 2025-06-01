package com.nycu.ce.ciphergame.backend.dao;

import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class GroupDAO {

    @PersistenceContext
    private EntityManager em;

    public Group getDetachedGroupById(GroupId id) {
        Group group = em.find(Group.class, id);
        em.detach(group);
        return group;
    }
}
