package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPermissionRequest {
    @NotBlank(message = "Permission name is required")
    @Size(max = 100)
    private String name;
 
    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    private String slug;
 
    @Size(max = 255)
    private String description;
 
    @NotNull(message = "Module is required")
    private PermissionModule module;
 
    @NotNull(message = "Action is required")
    private PermissionAction action;
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PermissionModule getModule() { return module; }
    public void setModule(PermissionModule module) { this.module = module; }
    public PermissionAction getAction() { return action; }
    public void setAction(PermissionAction action) { this.action = action; }
}
