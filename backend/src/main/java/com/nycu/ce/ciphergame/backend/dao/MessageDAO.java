package com.nycu.ce.ciphergame.backend.dao;

import org.springframework.stereotype.Repository;

import com.nycu.ce.ciphergame.backend.entity.Message;
import com.nycu.ce.ciphergame.backend.entity.id.MessageId;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class MessageDAO {

    @PersistenceContext
    private EntityManager em;

    public Message getDetachedMessageById(MessageId id) {
        Message message = em.find(Message.class, id);
        em.detach(message);
        return message;
    }
}
