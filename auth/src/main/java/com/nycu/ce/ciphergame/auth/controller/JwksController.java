package com.nycu.ce.ciphergame.auth.controller;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    @Autowired
    private Map<String, KeyPair> allKeys;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        List<JWK> jwks = allKeys.entrySet().stream()
                .map(entry -> new RSAKey.Builder((RSAPublicKey) entry.getValue().getPublic())
                .keyID(entry.getKey()) // This becomes the "kid"
                .algorithm(JWSAlgorithm.PS256)
                .keyUse(KeyUse.SIGNATURE)
                .build())
                .collect(Collectors.toList());

        return new JWKSet(jwks).toJSONObject(); // ✅ Exposes all keys in standard JWKS format
    }
}
