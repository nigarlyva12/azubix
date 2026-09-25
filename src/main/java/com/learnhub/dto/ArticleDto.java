package com.learnhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class ArticleDto {

    private Long id;

    @NotBlank(message = "Article title is required")
    private String title;

    private String content;

    
    private String titleDe;
    private String contentDe;

    @NotNull(message = "Please select a topic")
    private Long topicId;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTitleDe() { return titleDe; }
    public void setTitleDe(String titleDe) { this.titleDe = titleDe; }

    public String getContentDe() { return contentDe; }
    public void setContentDe(String contentDe) { this.contentDe = contentDe; }
}
