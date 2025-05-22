package com.nycu.ce.ciphergame.auth.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiKeyConfig {

    @Bean("userKeyPair")
    public KeyPair userKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @Bean("serviceKeyPair")
    public KeyPair serviceKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @Bean
    public Map<String, KeyPair> allKeys(
            @Qualifier("userKeyPair") KeyPair userKeyPair,
            @Qualifier("serviceKeyPair") KeyPair serviceKeyPair) {
        return Map.of(
                "user-key", userKeyPair,
                "service-key", serviceKeyPair);
    }
}
