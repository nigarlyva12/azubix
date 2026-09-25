package com.learnhub.controller;

import com.learnhub.entity.Category;
import com.learnhub.entity.User;
import com.learnhub.service.CategoryService;
import com.learnhub.service.CertificateService;
import com.learnhub.service.ProgressService;
import com.learnhub.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CertificateController {

    private final CertificateService certificateService;
    private final ProgressService    progressService;
    private final CategoryService    categoryService;
    private final UserService        userService;

    public CertificateController(CertificateService certificateService,
                                 ProgressService progressService,
                                 CategoryService categoryService,
                                 UserService userService) {
        this.certificateService = certificateService;
        this.progressService    = progressService;
        this.categoryService    = categoryService;
        this.userService        = userService;
    }

    @GetMapping("/certificate/{categoryId}")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long categoryId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = getLoggedInUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int progress = progressService.calculateCategoryProgress(user.getId(), categoryId);
        if (progress < 100) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Category category = categoryService.findById(categoryId);
        byte[] pdf = certificateService.generateCertificate(user, category);

        String slug = category.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        String filename = "certificate-" + slug + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private User getLoggedInUser(Authentication authentication) {
        String email;
        if (authentication.getPrincipal()
                instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }
        return email != null ? userService.findByEmail(email).orElse(null) : null;
    }
}
