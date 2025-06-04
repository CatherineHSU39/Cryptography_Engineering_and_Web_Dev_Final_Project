package com.nycu.ce.ciphergame.auth.security;

import java.security.KeyPair;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwtIssuer;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Autowired
    @Qualifier("serviceKeyPair")
    private KeyPair serviceKeyPair;

    @Autowired
    @Qualifier("userKeyPair")
    private KeyPair userKeyPair;

    public String generateServiceToken(String serviceName) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam("kid", "service-key") // MUST match JWKS
                .setSubject(serviceName)
                .setIssuer(jwtIssuer)
                .setAudience("kms")
                .claim("service_name", serviceName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(serviceKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateUserToken(CustomUserDetails userDetails, Boolean is2faVerified) {
        UUID id = userDetails.getId();
        String username = userDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam("kid", "user-key") // MUST match JWKS
                .setSubject(id.toString())
                .setIssuer(jwtIssuer)
                .setAudience("kms backend") // or use .claim("aud", List.of(...))
                .claim("username", username)
                .claim("role", authorities.stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .map(role -> role.replaceFirst("^ROLE_", ""))
                        .orElse("UNKNOWN"))
                .claim("2fa_verified", is2faVerified)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(userKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }
}
