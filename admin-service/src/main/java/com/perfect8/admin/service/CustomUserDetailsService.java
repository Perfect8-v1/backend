package com.perfect8.admin.service;

import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Check if account is active
        if (!user.isActive()) {
            log.warn("Attempt to load inactive user: {}", email);
            throw new UsernameNotFoundException("User account is not active: " + email);
        }

        // Check if account is locked
        if (user.isLocked()) {
            log.warn("Attempt to load locked user: {}", email);
            throw new UsernameNotFoundException("User account is locked: " + email);
        }

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());

        log.debug("User loaded successfully: {} with roles: {}", email, authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isLocked(), // accountNonLocked
                authorities
        );
    }
}
