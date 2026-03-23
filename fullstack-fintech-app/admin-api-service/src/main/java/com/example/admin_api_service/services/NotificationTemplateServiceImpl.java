package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.INotificationTemplateService;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationTemplateStatus;
import com.example.admin_api_service.enums.NotificationTemplateType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.notificationsAndComms.NotificationTemplate;
import com.example.admin_api_service.repository.NotificationTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class NotificationTemplateServiceImpl implements INotificationTemplateService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    private static final String FALLBACK_LOCALE = "en";

    private final NotificationTemplateRepository templateRepository;

    public NotificationTemplateServiceImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public NotificationTemplate createTemplate(NotificationTemplate template, String createdBy) {
        // Mirrors the DB unique constraint: template_key + channel + locale must be unique
        if (templateRepository.existsByTemplateKeyAndChannelAndLocale(
                template.getTemplateKey(), template.getChannel(), template.getLocale())) {
            throw new ConflictException("Template already exists for key='"
                    + template.getTemplateKey() + "' channel=" + template.getChannel()
                    + " locale=" + template.getLocale());
        }
        template.setStatus(NotificationTemplateStatus.ACTIVE);
        template.setCreatedBy(createdBy);
        // createdOn/updatedOn default to LocalDateTime.now() in the model
        return templateRepository.save(template);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public NotificationTemplate updateTemplate(String templateId, NotificationTemplate updated,
                                               String updatedBy) {
        NotificationTemplate existing = getTemplateById(templateId);

        // Transactional templates are core platform operations — protect them from edits
        if (existing.isTransactional()) {
            throw new ConflictException("Transactional templates cannot be modified");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setSubject(updated.getSubject());
        existing.setBodyText(updated.getBodyText());
        existing.setBodyHtml(updated.getBodyHtml());
        existing.setAvailableVariables(updated.getAvailableVariables());
        existing.setSenderName(updated.getSenderName());
        existing.setSenderAddress(updated.getSenderAddress());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return templateRepository.save(existing);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateById(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NotificationTemplate", "id", templateId));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplate(String templateKey, NotificationChannel channel,
                                            String locale) {
        return templateRepository
                .findByTemplateKeyAndChannelAndLocaleAndStatus(
                        templateKey, channel, locale, NotificationTemplateStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NotificationTemplate", "key+channel+locale",
                        templateKey + "+" + channel + "+" + locale));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateWithFallback(String templateKey,
                                                        NotificationChannel channel,
                                                        String locale) {
        return templateRepository
                .findByTemplateKeyAndChannelAndLocaleAndStatus(
                        templateKey, channel, locale, NotificationTemplateStatus.ACTIVE)
                .or(() -> templateRepository.findByTemplateKeyAndChannelAndLocaleAndStatus(
                        templateKey, channel, FALLBACK_LOCALE, NotificationTemplateStatus.ACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NotificationTemplate", "key+channel",
                        templateKey + "+" + channel));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplate> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByKey(String templateKey) {
        // Returns all channel + locale variants registered under this key
        return templateRepository.findAllByTemplateKey(templateKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByType(NotificationTemplateType type) {
        return templateRepository.findAllByTypeAndStatus(type, NotificationTemplateStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getTemplatesByChannel(NotificationChannel channel) {
        return templateRepository.findAllByChannelAndStatus(channel, NotificationTemplateStatus.ACTIVE);
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public String renderBody(String templateKey, NotificationChannel channel,
                             String locale, Map<String, String> variables) {
        NotificationTemplate template = getTemplateWithFallback(templateKey, channel, locale);
        return interpolate(template.getBodyText(), variables);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderSubject(String templateKey, NotificationChannel channel,
                                String locale, Map<String, String> variables) {
        NotificationTemplate template = getTemplateWithFallback(templateKey, channel, locale);
        if (template.getSubject() == null) return null;
        return interpolate(template.getSubject(), variables);
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    @Override
    public void activateTemplate(String templateId, String updatedBy) {
        NotificationTemplate template = getTemplateById(templateId);
        template.setStatus(NotificationTemplateStatus.ACTIVE);
        template.setUpdatedBy(updatedBy);
        template.setUpdatedOn(LocalDateTime.now());
        templateRepository.save(template);
    }

    @Override
    public void deactivateTemplate(String templateId, String updatedBy) {
        NotificationTemplate template = getTemplateById(templateId);
        // Transactional templates must always remain deliverable
        if (template.isTransactional()) {
            throw new ConflictException("Transactional templates cannot be deactivated");
        }
        template.setStatus(NotificationTemplateStatus.INACTIVE);
        template.setUpdatedBy(updatedBy);
        template.setUpdatedOn(LocalDateTime.now());
        templateRepository.save(template);
    }

    @Override
    public void archiveTemplate(String templateId, String updatedBy) {
        NotificationTemplate template = getTemplateById(templateId);
        template.setStatus(NotificationTemplateStatus.ARCHIVED);
        template.setUpdatedBy(updatedBy);
        template.setUpdatedOn(LocalDateTime.now());
        templateRepository.save(template);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteTemplate(String templateId) {
        NotificationTemplate template = getTemplateById(templateId);
        // Transactional templates underpin core platform flows — never allow hard delete
        if (template.isTransactional()) {
            throw new ConflictException("Transactional templates cannot be deleted");
        }
        if (template.getStatus() == NotificationTemplateStatus.ACTIVE) {
            throw new ConflictException(
                    "Active templates cannot be deleted. Archive or deactivate first.");
        }
        templateRepository.delete(template);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String interpolate(String text, Map<String, String> variables) {
        if (text == null || variables == null || variables.isEmpty()) return text;
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "{{" + key + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}