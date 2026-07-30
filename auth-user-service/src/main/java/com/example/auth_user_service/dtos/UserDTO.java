package com.example.auth_user_service.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.auth_user_service.models.Users;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private boolean enabled;
    private boolean twoFactorAuth;
    private List<UserRecordDTO> records;
    public UserDTO() {
    }

    public UserDTO(Long id, String email,
     String username, LocalDateTime createdOn, 
     LocalDateTime updatedOn, boolean enabled, boolean twoFactorAuth, List<UserRecordDTO> records) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.enabled = enabled;
        this.twoFactorAuth = twoFactorAuth; 
        this.records = records;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isTwoFactorAuth() { return twoFactorAuth; }
    public void setTwoFactorAuth(boolean twoFactorAuth) { this.twoFactorAuth = twoFactorAuth; } 
    public List<UserRecordDTO> getRecords() { return records; }
    public void setRecords(List<UserRecordDTO> records) { this.records = records; }

   public static UserDTO fromEntity(Users user) {
        UserDTO userDto = new UserDTO();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setEnabled(user.isEnabled());
        userDto.setCreatedOn(user.getCreatedOn());
        userDto.setUpdatedOn(user.getUpdatedOn());
        userDto.setTwoFactorAuth(user.isTwoFactorAuth());
        
        if (user.getRecords() != null) {
            List<UserRecordDTO> recordsDTOs = user.getRecords()
                    .stream()
                    .map(UserRecordDTO::fromEntity)
                    .collect(Collectors.toList());
            userDto.setRecords(recordsDTOs);
        } else {
            userDto.setRecords(new ArrayList<>());
        }
        return userDto;
    }
}