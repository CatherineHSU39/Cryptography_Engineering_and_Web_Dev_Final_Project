// package com.nycu.ce.ciphergame.backend.rls;

// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.util.UUID;

// import javax.sql.DataSource;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class RlsSessionFilter extends OncePerRequestFilter {

//     @Autowired
//     private RlsContextService rlsContextService;

//     @Autowired
//     private DataSource dataSource;

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain) throws ServletException, IOException {

//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//         if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
//             Jwt jwt = (Jwt) jwtAuth.getToken();
//             String userId = jwt.getClaimAsString("sub");
//             UUID uuid = UUID.fromString(userId);
//             rlsContextService.setCurrentUserId(uuid);

//             try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT set_config('app.current_user_id', ?, true)")) {
//                 stmt.setString(1, uuid.toString());
//                 stmt.execute();
//             } catch (Exception e) {
//                 throw new ServletException("Failed to set app.current_user", e);
//             }
//         }

//         try {
//             filterChain.doFilter(request, response);
//         } finally {
//             rlsContextService.clear();
//         }
//     }
// }
