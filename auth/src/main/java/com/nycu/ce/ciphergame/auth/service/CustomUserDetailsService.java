package com.nycu.ce.ciphergame.auth.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nycu.ce.ciphergame.auth.entity.User;
import com.nycu.ce.ciphergame.auth.repository.UserRepository;
import com.nycu.ce.ciphergame.auth.security.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        return new CustomUserDetails(user);
    }

    public CustomUserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with id: " + id);
        }
        return new CustomUserDetails(user);
    }
}
