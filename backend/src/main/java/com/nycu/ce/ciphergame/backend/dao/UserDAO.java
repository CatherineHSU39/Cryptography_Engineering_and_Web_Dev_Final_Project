package com.nycu.ce.ciphergame.backend.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class UserDAO {

    @PersistenceContext
    private EntityManager em;

    public User getDetachedUserById(UUID id) {
        User user = em.find(User.class, id);
        em.detach(user);
        return user;
    }
}
