package com.learnhub.service;

import com.learnhub.dto.UserRegistrationDto;
import com.learnhub.entity.Role;
import com.learnhub.entity.User;
import com.learnhub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for user-related business logic.
 * Handles registration, password encoding, and user lookups.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection (preferred over @Autowired on fields)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with the USER role.
     * Password is hashed using BCrypt before saving.
     *
     * @param dto the registration form data
     * @return the saved User entity
     * @throws RuntimeException if email is already taken
     */
    public User registerUser(UserRegistrationDto dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Create new user with encoded password
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Hash the password!
        user.setRole(Role.USER); // New registrations are always USER role

        return userRepository.save(user);
    }

    /**
     * Find a user by their email address.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by their ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
