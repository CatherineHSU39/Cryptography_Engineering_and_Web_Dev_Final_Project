package com.nycu.ce.ciphergame.backend.rls;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RlsSessionFilter extends OncePerRequestFilter {

    @Autowired
    private RlsContextService rlsContextService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            Jwt jwt = (Jwt) jwtAuth.getToken();
            String userId = jwt.getClaimAsString("sub"); // or "user_id" if that's your custom claim
            rlsContextService.setCurrentUserId(UUID.fromString(userId));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            rlsContextService.clear(); // optional but recommended
        }
    }
}
