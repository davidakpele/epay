package com.payrix.administrator.dtos;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.payrix.administrator.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class UserDTO implements UserDetails {
    private Long id;
    private String email;
    private String username;
    private String password;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private boolean enabled;
    private List<UserRecordDTO> records;
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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


    public UserDTO() {
    }

    public UserDTO(Long id, String email, String username, String password, LocalDateTime createdOn, LocalDateTime updatedOn, boolean enabled, List<UserRecordDTO> records, Role role) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.enabled = enabled;
        this.records = records;
        this.role = role;
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
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
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

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UserRecordDTO> getRecords() {
        return this.records;
    }

    public void setRecords(List<UserRecordDTO> records) {
        this.records = records;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
