package com.example.admin_api_service.models.accessAndSecurity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;

@Entity
@Table(name = "admin_permissions")
public class AdminPermission {

    @Id
    @Column(name = "id", columnDefinition = "char(36)", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // e.g. "wallets:freeze", "users:kyc:approve"
    @Column(name = "slug", nullable = false, length = 100, unique = true)
    private String slug;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", length = 50)
    private PermissionModule module;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20)
    private PermissionAction action;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    // ================= RELATIONSHIPS =================

    @OneToMany(mappedBy = "adminPermission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdminRolePermission> rolePermissions = new ArrayList<>();

    // ================= LIFECYCLE HOOKS =================

    @PrePersist
    public void prePersist() {
        this.createdOn = LocalDateTime.now();
    }

    public AdminPermission() {
    }

    public AdminPermission(String id, String name, String slug, String description, PermissionModule module, PermissionAction action, boolean isActive, LocalDateTime createdOn, List<AdminRolePermission> rolePermissions) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.module = module;
        this.action = action;
        this.isActive = isActive;
        this.createdOn = createdOn;
        this.rolePermissions = rolePermissions;
    }

    // ================= GETTERS & SETTERS =================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PermissionModule getModule() {
        return module;
    }

    public void setModule(PermissionModule module) {
        this.module = module;
    }

    public PermissionAction getAction() {
        return action;
    }

    public void setAction(PermissionAction action) {
        this.action = action;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public List<AdminRolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<AdminRolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
