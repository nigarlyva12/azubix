package com.learnhub.repository;

import com.learnhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * We only need to add custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Check if an email is already registered
    boolean existsByEmail(String email);
    
    Optional<User> findByOauthProviderAndOauthId(String provider, String oauthId);

    /** Top 20 users ranked by cached XP — used for the leaderboard. */
    java.util.List<User> findTop20ByOrderByTotalXpDesc();
}
