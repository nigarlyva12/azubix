package com.learnhub.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered user in the system.
 * Each user has an email, password, role, and a list of progress records.
 *
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false, unique = true) 
    private String email;

    @Column(nullable = false)
    private String password; 

	@Column(name = "oauth_provider")  
    private String oauthProvider;

    @Column(name = "oauth_id")  
    private String oauthId;
 
    private String name; 
    @Column(name = "profile_image_url")
    private String profileImageUrl; 

    @Column(name = "profile_image")
    private String profileImage; 
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

   
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Progress> progressList = new ArrayList<>();

    @Column(name = "last_active_date")
    private java.time.LocalDate lastActiveDate;

    @Column(name = "streak_count")
    private Integer streakCount = 0;

 
    @Column(name = "total_xp", nullable = false, columnDefinition = "integer not null default 0")
    private int totalXp = 0;
    


    public User() {
        
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Progress> getProgressList() {
        return progressList;
    }

    public void setProgressList(List<Progress> progressList) {
        this.progressList = progressList;
    }
    
    public String getOauthProvider() {
		return oauthProvider;
	}

	public void setOauthProvider(String oauthProvider) {
		this.oauthProvider = oauthProvider;
	}

	public String getOauthId() {
		return oauthId;
	}

	public void setOauthId(String oauthId) {
		this.oauthId = oauthId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public java.time.LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(java.time.LocalDate lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public Integer getStreakCount() { return streakCount; }
    public void setStreakCount(Integer streakCount) { this.streakCount = streakCount; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }
}
