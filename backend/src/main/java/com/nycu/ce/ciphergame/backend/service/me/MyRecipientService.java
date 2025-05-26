package com.nycu.ce.ciphergame.backend.service.me;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.backend.service.RecipientService;

@Service
public class MyRecipientService {

    @Autowired
    RecipientService recipientService;
}
