package com.learnhub.config;

import com.learnhub.entity.User;
import com.learnhub.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * adds the current user's email to every page's model,
 * so the navbar can display it regardless of login type.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        if (authentication.getPrincipal() instanceof OAuth2User) {
            return ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        }

        return authentication.getName();
    }
}