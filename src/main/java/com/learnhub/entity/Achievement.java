package com.learnhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 10)
    private String icon;
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    public Achievement() {}

    public Achievement(String key, String name, String description, String icon, int xpReward) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.xpReward = xpReward;
    }

    public Long getId()                     { return id; }
    public String getKey()                  { return key; }
    public void setKey(String key)          { this.key = key; }
    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }
    public String getDescription()          { return description; }
    public void setDescription(String d)    { this.description = d; }
    public String getIcon()                 { return icon; }
    public void setIcon(String icon)        { this.icon = icon; }
    public int getXpReward()               { return xpReward; }
    public void setXpReward(int xpReward)  { this.xpReward = xpReward; }
}
