package com.example.auth_user_service.configurations;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    private String uploadDir;
    private String maxFileSize;
    private List<String> allowedFileTypes;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(List<String> allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }
}