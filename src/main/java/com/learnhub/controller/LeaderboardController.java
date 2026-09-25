package com.learnhub.controller;

import com.learnhub.entity.User;
import com.learnhub.repository.UserAchievementRepository;
import com.learnhub.repository.UserRepository;
import com.learnhub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LeaderboardController {

    private final UserRepository            userRepository;
    private final UserAchievementRepository userAchievementRepo;
    private final UserService               userService;

    public LeaderboardController(UserRepository userRepository,
                                 UserAchievementRepository userAchievementRepo,
                                 UserService userService) {
        this.userRepository     = userRepository;
        this.userAchievementRepo = userAchievementRepo;
        this.userService        = userService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model, Authentication authentication) {

        List<User> topUsers = userRepository.findTop20ByOrderByTotalXpDesc();

        List<Map<String, Object>> entries = new ArrayList<>();
        int rank = 1;
        for (User u : topUsers) {
            if (u.getTotalXp() == 0) continue;          // skip users with no XP
            Map<String, Object> row = new HashMap<>();
            row.put("rank",       rank);
            row.put("user",       u);
            row.put("xp",         u.getTotalXp());
            row.put("badges",     userAchievementRepo.countByUser(u));
            row.put("displayName", resolveDisplayName(u));
            entries.add(row);
            rank++;
        }
        model.addAttribute("entries", entries);

        if (authentication != null && authentication.isAuthenticated()) {
            String email;
            if (authentication.getPrincipal()
                    instanceof org.springframework.security.oauth2.core.user.OAuth2User o) {
                email = o.getAttribute("email");
            } else {
                email = authentication.getName();
            }
            userService.findByEmail(email)
                       .ifPresent(u -> model.addAttribute("currentUserId", u.getId()));
        }

        return "leaderboard";
    }

    private String resolveDisplayName(User user) {
        if (user.getName() != null && !user.getName().isBlank()) return user.getName();
        String email = user.getEmail();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
