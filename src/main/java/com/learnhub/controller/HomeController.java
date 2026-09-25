package com.learnhub.controller;

import com.learnhub.entity.Article;
import com.learnhub.entity.Category;
import com.learnhub.entity.Topic;
import com.learnhub.entity.User;
import com.learnhub.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final TopicService    topicService;
    private final ArticleService  articleService;
    private final ProgressService progressService;
    private final UserService     userService;
    private final ActivityService activityService;
    private final CommentService  commentService;
    private final XpService       xpService;

    public HomeController(CategoryService categoryService,
                          TopicService topicService,
                          ArticleService articleService,
                          ProgressService progressService,
                          UserService userService,
                          ActivityService activityService,
                          CommentService commentService,
                          XpService xpService) {
        this.categoryService = categoryService;
        this.topicService    = topicService;
        this.articleService  = articleService;
        this.progressService = progressService;
        this.userService     = userService;
        this.activityService = activityService;
        this.commentService  = commentService;
        this.xpService       = xpService;
    }

    // ── Home ─────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String homePage(Model model, Authentication authentication, Locale locale) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

   
        String lang = locale.getLanguage();
        List<Article> recent = articleService.findRecent();
        Map<Long, String> feedTitleMap  = new HashMap<>();
        Map<Long, String> feedTeaserMap = new HashMap<>();
        for (Article a : recent) {
            feedTitleMap.put(a.getId(),  a.getLocalizedTitle(lang));
            feedTeaserMap.put(a.getId(), articleService.generateTeaser(a.getLocalizedContent(lang), 115));
        }
        model.addAttribute("recentArticles", recent);
        model.addAttribute("feedTitleMap",   feedTitleMap);
        model.addAttribute("feedTeaserMap",  feedTeaserMap);

        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                Map<Long, Integer> progressMap = new HashMap<>();
                for (Category category : categories) {
                    progressMap.put(category.getId(),
                            progressService.calculateCategoryProgress(user.getId(), category.getId()));
                }
                model.addAttribute("progressMap", progressMap);
                xpService.awardDailyLoginIfNeeded(user);
            }
        }
        return "home";
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    public String categoriesPage(Model model, Authentication authentication) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                Map<Long, Integer> progressMap = new HashMap<>();
                List<Category> completedCategories = new ArrayList<>();
                for (Category category : categories) {
                    int progress = progressService.calculateCategoryProgress(
                            user.getId(), category.getId());
                    progressMap.put(category.getId(), progress);
                    if (progress == 100 && !category.getTopics().isEmpty()) {
                        completedCategories.add(category);
                    }
                }
                model.addAttribute("progressMap", progressMap);
                model.addAttribute("completedCategories", completedCategories);
            }
        }
        return "categories";
    }

    // ── Category detail ───────────────────────────────────────────────────────

    @GetMapping("/category/{id}")
    public String categoryPage(@PathVariable Long id, Model model,
                               Authentication authentication) {
        Category category = categoryService.findById(id);
        List<Topic> topics = topicService.findByCategoryId(id);

        model.addAttribute("category", category);
        model.addAttribute("topics", topics);
        model.addAttribute("categories", categoryService.findAll());

        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                model.addAttribute("completedTopicIds",
                        progressService.getCompletedTopicIds(user.getId()));
            }
        }
        return "topics";
    }

    // ── Topic detail ──────────────────────────────────────────────────────────

    @GetMapping("/topic/{id}")
    public String topicPage(@PathVariable Long id, Model model,
                            Authentication authentication, Locale locale) {
        Topic topic    = topicService.findById(id);
        List<Article> articles = articleService.findByTopicId(id);

        model.addAttribute("topic", topic);
        model.addAttribute("articles", articles);
        model.addAttribute("categories", categoryService.findAll());

        String lang = locale.getLanguage();   

        Map<Long, String> localizedTitleMap = new HashMap<>();
        Map<Long, Integer> readingTimeMap   = new HashMap<>();
        for (Article a : articles) {
            localizedTitleMap.put(a.getId(), a.getLocalizedTitle(lang));
            readingTimeMap.put(a.getId(), articleService.calculateReadingTime(a.getLocalizedContent(lang)));
        }
        model.addAttribute("localizedTitleMap", localizedTitleMap);
        model.addAttribute("readingTimeMap", readingTimeMap);

        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                model.addAttribute("isCompleted",
                        progressService.isTopicCompleted(user.getId(), id));
            }
        }
        return "topic-detail";
    }

    // ── Article ───────────────────────────────────────────────────────────────

    @GetMapping("/article/{id}")
    public String articlePage(@PathVariable Long id, Model model,
                              Authentication authentication, Locale locale) {
        Article article = articleService.findById(id);
        String lang = locale.getLanguage();


        model.addAttribute("article", article);
        model.addAttribute("localizedTitle",   article.getLocalizedTitle(lang));
        model.addAttribute("localizedContent", article.getLocalizedContent(lang));
        model.addAttribute("hasDeTranslation",
                article.getTitleDe() != null && !article.getTitleDe().isBlank());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("readingTime",
                articleService.calculateReadingTime(article.getLocalizedContent(lang)));

        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                model.addAttribute("isCompleted",
                        progressService.isTopicCompleted(user.getId(), article.getTopic().getId()));

                activityService.logActivity(user, "READ",
                        article.getTitle(), "/article/" + article.getId());

                xpService.awardArticleRead(user, article.getId());

                model.addAttribute("currentUserId", user.getId());
                model.addAttribute("currentUserRole", user.getRole().name());
            }
        }

        model.addAttribute("comments", commentService.getCommentsForArticle(id));
        model.addAttribute("commentCount", commentService.countComments(id));

        return "article";
    }


    @PostMapping("/progress/toggle/{topicId}")
    public String toggleProgress(@PathVariable Long topicId,
                                 Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = getLoggedInUser(authentication);
            if (user != null) {
                progressService.toggleTopicCompletion(user.getId(), topicId);

                Topic topic = topicService.findById(topicId);

         
                if (progressService.isTopicCompleted(user.getId(), topicId)) {
                    xpService.awardTopicComplete(user, topic);
                    activityService.logActivity(user, "COMPLETED",
                            topic.getTitle(), "/topic/" + topicId);
                }
            }
        }
        return "redirect:/topic/" + topicId;
    }

    private User getLoggedInUser(Authentication authentication) {
        String email;
        if (authentication.getPrincipal()
                instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }
        return userService.findByEmail(email).orElse(null);
    }
}
