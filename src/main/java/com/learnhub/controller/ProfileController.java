package com.learnhub.controller;

import com.learnhub.entity.Category;
import com.learnhub.entity.User;
import com.learnhub.repository.UserRepository;
import com.learnhub.service.CategoryService;
import com.learnhub.service.ProgressService;
import com.learnhub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final ProgressService progressService;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public ProfileController(UserService userService,
                             UserRepository userRepository,
                             CategoryService categoryService,
                             ProgressService progressService,
                             PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.progressService = progressService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String profilePage(Authentication authentication, Model model) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        List<Category> categories = categoryService.findAll();
        Map<String, Integer> progressMap = new HashMap<>();
        int totalProgress = 0;
        int categoryCount = 0;

        for (Category category : categories) {
            int progress = progressService.calculateCategoryProgress(
                    user.getId(), category.getId());
            progressMap.put(category.getName(), progress);
            totalProgress += progress;
            categoryCount++;
        }

        model.addAttribute("progressMap", progressMap);
        model.addAttribute("overallProgress",
                categoryCount > 0 ? totalProgress / categoryCount : 0);
        model.addAttribute("categories", categories);

        model.addAttribute("isOAuthUser",
                user.getOauthProvider() != null && !user.getOauthProvider().isEmpty());

        return "profile";
    }

    /**
     * update user's name.
     */
    @PostMapping("/update-name")
    public String updateName(@RequestParam String name,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        user.setName(name.trim());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Name updated successfully!");

        return "redirect:/profile";
    }

    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        if (user.getOauthProvider() != null && !user.getOauthProvider().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Password cannot be changed for Google accounts.");
            return "redirect:/profile";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error",
                    "Password must be at least 6 characters.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");

        return "redirect:/profile";
    }

   
    @PostMapping("/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile file,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select an image.");
            return "redirect:/profile";
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "Only image files are allowed.");
            return "redirect:/profile";
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String extension = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().substring(
                        file.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());

            if (user.getProfileImage() != null) {
                Path oldPath = Paths.get(UPLOAD_DIR + user.getProfileImage());
                Files.deleteIfExists(oldPath);
            }

            user.setProfileImage(filename);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile image updated!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
        }

        return "redirect:/profile";
    }

    @PostMapping("/delete-image")
    public String deleteImage(Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = getUser(authentication);
        if (user == null) return "redirect:/login";

        if (user.getProfileImage() != null) {
            
            try {
                Path filePath = Paths.get(UPLOAD_DIR + user.getProfileImage());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
               
            }

            user.setProfileImage(null);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile image removed.");
        }

        return "redirect:/profile";
    }

    private User getUser(Authentication authentication) {
        if (authentication == null) return null;

        String email;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        } else {
            email = authentication.getName();
        }

        return userService.findByEmail(email).orElse(null);
    }
}