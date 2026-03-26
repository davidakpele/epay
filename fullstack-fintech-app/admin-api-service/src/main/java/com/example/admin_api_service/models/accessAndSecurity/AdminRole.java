package com.example.admin_api_service.models.accessAndSecurity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.models.AdminUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "admin_roles")
public class AdminRole {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
   
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdminRolePermission> rolePermissions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "role")
    private List<AdminUser> adminUsers = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public AdminRole() {
    }

    public AdminRole(String id, String name, String description, boolean isSystem, boolean isActive, String createdBy, LocalDateTime createdOn, LocalDateTime updatedOn, List<AdminRolePermission> rolePermissions, List<AdminUser> adminUsers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isSystem = isSystem;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.rolePermissions = rolePermissions;
        this.adminUsers = adminUsers;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public List<AdminRolePermission> getRolePermissions() { return rolePermissions; }
    public void setRolePermissions(List<AdminRolePermission> rolePermissions) { this.rolePermissions = rolePermissions; }

    public List<AdminUser> getAdminUsers() { return adminUsers; }
    public void setAdminUsers(List<AdminUser> adminUsers) { this.adminUsers = adminUsers; }
}