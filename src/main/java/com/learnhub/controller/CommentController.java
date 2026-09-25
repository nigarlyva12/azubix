package com.learnhub.controller;

import com.learnhub.entity.User;
import com.learnhub.service.CommentService;
import com.learnhub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping("/article/{articleId}/comment")
    public String addComment(@PathVariable Long articleId,
                             @RequestParam String content,
                             Authentication authentication) {
        if (content == null || content.isBlank()) {
            return "redirect:/article/" + articleId + "#comments";
        }
        User user = getUser(authentication);
        if (user != null) {
            commentService.addComment(user, articleId, content);
        }
        return "redirect:/article/" + articleId + "#comments";
    }

    @PostMapping("/comment/{commentId}/reply")
    public String addReply(@PathVariable Long commentId,
                           @RequestParam String content,
                           @RequestParam Long articleId,
                           Authentication authentication) {
        if (content == null || content.isBlank()) {
            return "redirect:/article/" + articleId + "#comments";
        }
        User user = getUser(authentication);
        if (user != null) {
            commentService.addReply(user, commentId, content);
        }
        return "redirect:/article/" + articleId + "#comments";
    }
    
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                Authentication authentication) {
        User user = getUser(authentication);
        if (user != null) {
            Long articleId = commentService.deleteComment(commentId, user);
            return "redirect:/article/" + articleId + "#comments";
        }
        return "redirect:/";
    }

    private User getUser(Authentication authentication) {
        if (authentication == null) return null;
        String email;
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }
        return userService.findByEmail(email).orElse(null);
    }
}
