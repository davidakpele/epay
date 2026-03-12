package com.example.administrator_api.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.administrator_api.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Table("administrator_users")
public class User implements UserDetails {

    @Id
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 100)
    @Column("username")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150)
    @Column("email")
    private String email;

    @NotBlank(message = "Password is required")
    @Column("password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Size(max = 100)
    @Column("first_name")
    private String firstName;

    @Size(max = 100)
    @Column("last_name")
    private String lastName;

    @Column("role")
    private Role role;

    @Column("enabled")
    private boolean enabled = true;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /* ========= Constructors ========= */

    public User() {
    }

    public static User create(String username, String email, String encodedPassword,
                              String firstName, String lastName, Role role) {
        User user = new User();
        user.username = sanitizeInput(username);
        user.email = sanitizeInput(email);
        user.password = encodedPassword;
        user.firstName = firstName;
        user.lastName = lastName;
        user.role = role;
        user.enabled = true;
        return user;
    }

    /* ========= Sanitization ========= */
    // Moved to static — called explicitly on create/update
    // since R2DBC has no @PrePersist/@PreUpdate lifecycle hooks

    public static String sanitizeInput(String input) {
        if (input == null) return null;
        String sanitized = input
                .replaceAll("<script.*?>.*?</script>", "")
                .replaceAll("javascript:", "")
                .replaceAll("onerror=", "")
                .replaceAll("onload=", "")
                .replaceAll("onclick=", "")
                .replaceAll("eval\\(", "");
        sanitized = sanitized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
        return sanitized.trim();
    }

    /* ========= Business Methods ========= */

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        if (email != null && !email.trim().isEmpty()) {
            this.email = sanitizeInput(email);
        }
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    /* ========= Spring Security ========= */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return this.enabled; }

    /* ========= Getters & Setters ========= */

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    @Override
    public String getPassword()                  { return password; }
    
    public void setPassword(String password)     { this.password = password; }

    public String getFirstName()                 { return firstName; }
    public void setFirstName(String firstName)   { this.firstName = firstName; }

    public String getLastName()                  { return lastName; }
    public void setLastName(String lastName)     { this.lastName = lastName; }

    public Role getRole()                        { return role; }
    public void setRole(Role role)               { this.role = role; }

    public boolean getEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)      { this.enabled = enabled; }

    public LocalDateTime getCreatedAt()                      { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)        { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                      { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)        { this.updatedAt = updatedAt; }

    /* ========= Equals, HashCode, ToString ========= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "User{id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role=" + role +
               ", enabled=" + enabled + '}';
    }
}