package com.nycu.ce.ciphergame.backend.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Message;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class MessageDAO {

    @PersistenceContext
    private EntityManager em;

    public Message getDetachedMessageById(UUID id) {
        Message message = em.find(Message.class, id);
        em.detach(message);
        return message;
    }
}
