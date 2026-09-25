package com.learnhub.controller;

import com.learnhub.dto.ArticleDto;
import com.learnhub.dto.CategoryDto;
import com.learnhub.dto.TopicDto;
import com.learnhub.entity.*;
import com.learnhub.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;


/**
 * all routes start with /admin/** and are protected via SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CategoryService  categoryService;
    private final TopicService     topicService;
    private final ArticleService   articleService;
    private final ExamService      examService;
    private final FlashcardService flashcardService;
    private final ObjectMapper     objectMapper;

    public AdminController(CategoryService categoryService,
                           TopicService topicService,
                           ArticleService articleService,
                           ExamService examService,
                           FlashcardService flashcardService,
                           ObjectMapper objectMapper) {
        this.categoryService  = categoryService;
        this.topicService     = topicService;
        this.articleService   = articleService;
        this.examService      = examService;
        this.flashcardService = flashcardService;
        this.objectMapper     = objectMapper;
    }

    // ========================================================================
    // DASHBOARD
    // ========================================================================

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("categoryCount", categoryService.findAll().size());
        model.addAttribute("topicCount", topicService.findAll().size());
        model.addAttribute("articleCount", articleService.findAll().size());
        return "admin/dashboard"; 
    }

    // ========================================================================
    // CATEGORY CRUD
    // ========================================================================

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryDto", new CategoryDto()); 
        return "admin/categories";
    }

    @PostMapping("/categories/create")
    public String createCategory(@Valid @ModelAttribute("categoryDto") CategoryDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/categories";
        }

        try {
            categoryService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Category created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        model.addAttribute("categoryDto", dto);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editMode", true);

        return "admin/categories";
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryDto") CategoryDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editMode", true);
            return "admin/categories";
        }

        try {
            categoryService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/categories";
    }


    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete category. It may have associated topics.");
        }

        return "redirect:/admin/categories";
    }

    // ========================================================================
    // TOPIC CRUD
    // ========================================================================

    @GetMapping("/topics")
    public String listTopics(Model model) {
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("topicDto", new TopicDto());
        return "admin/topics";
    }

    @PostMapping("/topics/create")
    public String createTopic(@Valid @ModelAttribute("topicDto") TopicDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/topics";
        }

        try {
            topicService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Topic created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/topics";
    }

    @GetMapping("/topics/edit/{id}")
    public String editTopicForm(@PathVariable Long id, Model model) {
        Topic topic = topicService.findById(id);

        TopicDto dto = new TopicDto();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setCategoryId(topic.getCategory().getId());

        model.addAttribute("topicDto", dto);
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editMode", true);

        return "admin/topics";
    }

    @PostMapping("/topics/update/{id}")
    public String updateTopic(@PathVariable Long id,
                              @Valid @ModelAttribute("topicDto") TopicDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editMode", true);
            return "admin/topics";
        }

        try {
            topicService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "Topic updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/topics";
    }

    @PostMapping("/topics/delete/{id}")
    public String deleteTopic(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            topicService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Topic deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete topic. It may have associated articles.");
        }

        return "redirect:/admin/topics";
    }

    // ========================================================================
    // ARTICLE CRUD
    // ========================================================================

    @GetMapping("/articles")
    public String listArticles(Model model) {
        model.addAttribute("articles", articleService.findAll());
        return "admin/articles";
    }

    @GetMapping("/articles/create")
    public String createArticleForm(Model model) {
        model.addAttribute("articleDto", new ArticleDto());
        model.addAttribute("topics", topicService.findAll());
        return "admin/article-form";
    }

    @PostMapping("/articles/create")
    public String createArticle(@Valid @ModelAttribute("articleDto") ArticleDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
    	// Add this right after the result.hasErrors() check
    	if (dto.getContent() == null || dto.getContent().isBlank()
    	        || dto.getContent().equals("<p><br></p>")) {
    	    model.addAttribute("topics", topicService.findAll());
    	    model.addAttribute("error", "Article content cannot be empty.");
    	    return "admin/article-form";
    	}
        if (result.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            return "admin/article-form";
        }

        try {
            articleService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Article created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/articles";
    }

    @GetMapping("/articles/edit/{id}")
    public String editArticleForm(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);

        ArticleDto dto = new ArticleDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setTitleDe(article.getTitleDe());
        dto.setContentDe(article.getContentDe());
        dto.setTopicId(article.getTopic().getId());

        model.addAttribute("articleDto", dto);
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("editMode", true);

        return "admin/article-form";
    }

    @PostMapping("/articles/update/{id}")
    public String updateArticle(@PathVariable Long id,
                                @Valid @ModelAttribute("articleDto") ArticleDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("topics", topicService.findAll());
            model.addAttribute("editMode", true);
            return "admin/article-form";
        }

        try {
            articleService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "Article updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/articles";
    }
    
    @PostMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Article deleted successfully!");
        return "redirect:/admin/articles";
    }

    // ========================================================================
    // EXAM QUESTIONS
    // ========================================================================

    @GetMapping("/topics/{topicId}/questions")
    public String listQuestions(@PathVariable Long topicId, Model model) {
        Topic topic = topicService.findById(topicId);
        List<Question> questions = examService.getQuestionsForTopic(topicId);
        model.addAttribute("topic",     topic);
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new Question());
        model.addAttribute("questionTypes", Question.QuestionType.values());
        return "admin/exam-questions";
    }

    @PostMapping("/topics/{topicId}/questions/create")
    public String createQuestion(@PathVariable Long topicId,
                                 @RequestParam String text,
                                 @RequestParam Question.QuestionType type,
                                 @RequestParam(required = false) String[] options,
                                 @RequestParam String correctAnswer,
                                 @RequestParam(required = false) String explanation,
                                 RedirectAttributes ra) {
        Topic topic = topicService.findById(topicId);
        Question q = new Question();
        q.setTopic(topic);
        q.setText(text);
        q.setType(type);
        q.setCorrectAnswer(correctAnswer);
        q.setExplanation(explanation);
        q.setOrderIndex(examService.getQuestionsForTopic(topicId).size());

        if (type == Question.QuestionType.MULTIPLE_CHOICE && options != null) {
            try {
                q.setOptionsJson(objectMapper.writeValueAsString(Arrays.asList(options)));
            } catch (Exception ignored) {}
        }

        examService.saveQuestion(q);
        ra.addFlashAttribute("success", "Question added!");
        return "redirect:/admin/topics/" + topicId + "/questions";
    }

    @PostMapping("/topics/{topicId}/questions/delete/{questionId}")
    public String deleteQuestion(@PathVariable Long topicId,
                                 @PathVariable Long questionId,
                                 RedirectAttributes ra) {
        examService.deleteQuestion(questionId);
        ra.addFlashAttribute("success", "Question deleted.");
        return "redirect:/admin/topics/" + topicId + "/questions";
    }

    // ========================================================================
    // FLASHCARDS
    // ========================================================================

    @GetMapping("/topics/{topicId}/flashcards")
    public String listFlashcards(@PathVariable Long topicId, Model model) {
        Topic topic = topicService.findById(topicId);
        List<Flashcard> cards = flashcardService.getFlashcardsForTopic(topicId);
        model.addAttribute("topic", topic);
        model.addAttribute("cards", cards);
        return "admin/flashcards-admin";
    }

    @PostMapping("/topics/{topicId}/flashcards/create")
    public String createFlashcard(@PathVariable Long topicId,
                                  @RequestParam String front,
                                  @RequestParam String back,
                                  RedirectAttributes ra) {
        Topic topic = topicService.findById(topicId);
        Flashcard card = new Flashcard();
        card.setTopic(topic);
        card.setFront(front);
        card.setBack(back);
        card.setOrderIndex(flashcardService.getFlashcardsForTopic(topicId).size());
        flashcardService.saveFlashcard(card);
        ra.addFlashAttribute("success", "Flashcard added!");
        return "redirect:/admin/topics/" + topicId + "/flashcards";
    }

    @PostMapping("/topics/{topicId}/flashcards/delete/{cardId}")
    public String deleteFlashcard(@PathVariable Long topicId,
                                  @PathVariable Long cardId,
                                  RedirectAttributes ra) {
        flashcardService.deleteFlashcard(cardId);
        ra.addFlashAttribute("success", "Flashcard deleted.");
        return "redirect:/admin/topics/" + topicId + "/flashcards";
    }
}
