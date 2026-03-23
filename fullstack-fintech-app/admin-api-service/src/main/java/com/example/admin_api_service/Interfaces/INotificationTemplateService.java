package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationTemplateType;
import com.example.admin_api_service.models.notificationsAndComms.NotificationTemplate;

public interface INotificationTemplateService {
    NotificationTemplate createTemplate(NotificationTemplate template, String createdBy);
 
    NotificationTemplate updateTemplate(String templateId, NotificationTemplate updated, String updatedBy);
 
    NotificationTemplate getTemplateById(String templateId);
 
    NotificationTemplate getTemplate(String templateKey, NotificationChannel channel, String locale);
 
    // Falls back to "en" locale if the requested locale is not found
    NotificationTemplate getTemplateWithFallback(String templateKey, NotificationChannel channel,
                                                  String locale);
 
    Page<NotificationTemplate> getAllTemplates(Pageable pageable);
 
    List<NotificationTemplate> getTemplatesByKey(String templateKey);
 
    List<NotificationTemplate> getTemplatesByType(NotificationTemplateType type);
 
    List<NotificationTemplate> getTemplatesByChannel(NotificationChannel channel);
 
    // Render a template by substituting variables into the body
    String renderBody(String templateKey, NotificationChannel channel,
                      String locale, Map<String, String> variables);
 
    String renderSubject(String templateKey, NotificationChannel channel,
                         String locale, Map<String, String> variables);
 
    void activateTemplate(String templateId, String updatedBy);
 
    void deactivateTemplate(String templateId, String updatedBy);
 
    void archiveTemplate(String templateId, String updatedBy);
 
    void deleteTemplate(String templateId);
}
