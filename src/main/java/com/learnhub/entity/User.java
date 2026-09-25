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
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    private Long id;

    @Column(nullable = false, unique = true) // Email must be unique
    private String email;

    @Column(nullable = false)
    private String password; // Stored as a BCrypt hash, never plain text

	@Column(name = "oauth_provider")  //google, github or facebook
    private String oauthProvider;

    @Column(name = "oauth_id")   // user's unique id from google etc.
    private String oauthId;
 
    private String name; // user's full name from google provider
    @Column(name = "profile_image_url")
    private String profileImageUrl; // Google profile picture URL

    @Column(name = "profile_image")
    private String profileImage; // Stores the filename of uploaded image
    
    @Enumerated(EnumType.STRING) // Store role as "ADMIN" or "USER" string in DB
    @Column(nullable = false)
    private Role role;

    // One user can have many progress records (one per topic)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Progress> progressList = new ArrayList<>();
    
    //User-Dashboard
    @Column(name = "last_active_date")
    private java.time.LocalDate lastActiveDate;

    @Column(name = "streak_count")
    private Integer streakCount = 0;

    /** Cached total XP — updated atomically alongside every XpEvent insert. */
    @Column(name = "total_xp", nullable = false, columnDefinition = "integer not null default 0")
    private int totalXp = 0;
    
    // --- Constructors ---

    public User() {
        // Default constructor 
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

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
