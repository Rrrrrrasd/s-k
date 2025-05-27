package com.contract.backend.common.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @jakarta.persistence.Column(nullable = false, unique = true ,columnDefinition = "char(36)", length = 36)
    private String uuid;

    @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserEntity() {}

    public UserEntity(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.uuid = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    //Getter
    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUuid() { return uuid; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    //Setter
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setUuid(String uuid) { this.uuid = uuid; }

}
