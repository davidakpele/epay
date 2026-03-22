package com.example.admin_api_service.models.accessAndSecurity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "admin_role_permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"})
)
public class AdminRolePermission {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "role_id", length = 36, nullable = false)
    private String roleId;

    @Column(name = "permission_id", length = 36, nullable = false)
    private String permissionId;

    @Column(name = "granted_by", length = 36)
    private String grantedBy;

    @Column(name = "granted_on", nullable = false, updatable = false)
    private LocalDateTime grantedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private AdminRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private AdminPermission permission;

    public AdminRolePermission() {
    }

    public AdminRolePermission(String id, String roleId, String permissionId, String grantedBy, LocalDateTime grantedOn, AdminRole role, AdminPermission permission) {
        this.id = id;
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.grantedBy = grantedBy;
        this.grantedOn = grantedOn;
        this.role = role;
        this.permission = permission;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }

    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }

    public LocalDateTime getGrantedOn() { return grantedOn; }
    public void setGrantedOn(LocalDateTime grantedOn) { this.grantedOn = grantedOn; }

    public AdminRole getRole() { return role; }
    public void setRole(AdminRole role) { this.role = role; }

    public AdminPermission getPermission() { return permission; }
    public void setPermission(AdminPermission permission) { this.permission = permission; }
}