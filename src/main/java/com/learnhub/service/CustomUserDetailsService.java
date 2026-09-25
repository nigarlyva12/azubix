package com.learnhub.service;

import com.learnhub.entity.User;
import com.learnhub.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * This service tells Spring Security how to load a user from our database.
 * Spring Security calls this automatically during login.
 *
 * It implements UserDetailsService which is Spring Security's interface
 * for fetching user data during authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by Spring Security when a user tries to log in.
     * The "username" parameter is actually the email in our case.
     *
     * We convert our User entity into Spring Security's UserDetails object
     * which contains: username (email), password (hash), and authorities (roles).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Look up the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Convert our Role enum to a Spring Security authority
        // "ROLE_" prefix is required by Spring Security convention
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Return Spring Security's User object (not our entity!)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}
