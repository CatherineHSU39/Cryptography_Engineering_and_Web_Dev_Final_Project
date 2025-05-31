package com.nycu.ce.ciphergame.dek.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// import com.nycu.ce.ciphergame.dek.rls.RlsSessionFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // @Autowired
    // private RlsSessionFilter rlsSessionFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                );
        // .addFilterAfter(rlsSessionFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
