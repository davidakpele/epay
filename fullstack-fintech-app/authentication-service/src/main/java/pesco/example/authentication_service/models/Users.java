package pesco.example.authentication_service.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import pesco.example.authentication_service.enums.Role;

@Data
@Builder
@Entity
@Table(name = "users")
public class Users implements UserDetails {
    @Id
    private Long id;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String username;

    private boolean twoFactorAuth;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<UserRecord> records;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Users() {
    }

    public Users(Long id, String email, String password, String username, boolean twoFactorAuth, LocalDateTime createdOn, LocalDateTime updatedOn, Role role, boolean enabled, List<UserRecord> records) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.twoFactorAuth = twoFactorAuth;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.role = role;
        this.enabled = enabled;
        this.records = records;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isTwoFactorAuth() {
        return this.twoFactorAuth;
    }

    public boolean getTwoFactorAuth() {
        return this.twoFactorAuth;
    }

    public void setTwoFactorAuth(boolean twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    public LocalDateTime getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UserRecord> getRecords() {
        return this.records;
    }

    public void setRecords(List<UserRecord> records) {
        this.records = records;
    }

    @PrePersist
    @PreUpdate
    protected void beforeSaveOrUpdate() {
        // Sanitize inputs
        this.email = sanitizeInput(this.email);
        this.username = sanitizeInput(this.username);
        
    }


    private String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove script tags
        String sanitized = input.replaceAll("<script.*?>.*?</script>", "")
                               .replaceAll("javascript:", "")
                               .replaceAll("onerror=", "")
                               .replaceAll("onload=", "")
                               .replaceAll("onclick=", "")
                               .replaceAll("eval\\(", "");
        
        // Escape HTML entities
        sanitized = sanitized.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#x27;");
        
        return sanitized.trim();
    }

}