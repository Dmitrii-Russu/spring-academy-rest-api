package com.example.oauth_secured_rest_api.user;

import com.example.oauth_secured_rest_api.role.Role;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class UserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    protected UserEntity() {}
    public UserEntity(
            String username,
            String password,
            Set<Role> roles
    ) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public Long getId() { return id;}
    public String getUsername() { return username;}
    public void setUsername(String username) { this.username = username;}
    public String getPassword() { return password;}
    public void setPassword(String password) { this.password = password;}
    public Set<Role> getRoles() { return roles;}
}