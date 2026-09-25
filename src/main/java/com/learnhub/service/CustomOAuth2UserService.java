package com.learnhub.service;

import com.learnhub.entity.Role;
import com.learnhub.entity.User;
import com.learnhub.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles Google OAuth2 login.
 * When a user logs in with Google, this service:
 * 1. Checks if the user already exists in our database
 * 2. If not, creates a new account automatically
 * 3. Returns the OAuth2User for Spring Security
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String picture = oAuth2User.getAttribute("picture"); // Google profile image URL
        String provider = userRequest.getClientRegistration().getRegistrationId();

        Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(provider, googleId);

        if (existingUser.isEmpty()) {
            Optional<User> emailUser = userRepository.findByEmail(email);

            if (emailUser.isPresent()) {
                User user = emailUser.get();
                user.setOauthProvider(provider);
                user.setOauthId(googleId);
                if (user.getName() == null) user.setName(name);
                user.setProfileImageUrl(picture); // Save Google picture
                userRepository.save(user);
            } else {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword("");
                newUser.setName(name);
                newUser.setRole(Role.USER);
                newUser.setOauthProvider(provider);
                newUser.setOauthId(googleId);
                newUser.setProfileImageUrl(picture); // Save Google picture
                userRepository.save(newUser);
            }
        } else {
            // Update picture on every login (Google may change it)
            User user = existingUser.get();
            user.setProfileImageUrl(picture);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}