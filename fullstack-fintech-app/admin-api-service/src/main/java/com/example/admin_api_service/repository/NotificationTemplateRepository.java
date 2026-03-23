package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationTemplateStatus;
import com.example.admin_api_service.enums.NotificationTemplateType;
import com.example.admin_api_service.models.notificationsAndComms.NotificationTemplate;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
         // Used by createTemplate() to enforce the unique constraint at service level
    boolean existsByTemplateKeyAndChannelAndLocale(
            String templateKey, NotificationChannel channel, String locale);

    // Used by getTemplate() — exact locale match, active only
    Optional<NotificationTemplate> findByTemplateKeyAndChannelAndLocaleAndStatus(
            String templateKey, NotificationChannel channel,
            String locale, NotificationTemplateStatus status);

    // Used by getTemplatesByKey() — all locales and channels for a key
    List<NotificationTemplate> findAllByTemplateKey(String templateKey);

    // Used by getTemplatesByType()
    List<NotificationTemplate> findAllByTypeAndStatus(
            NotificationTemplateType type, NotificationTemplateStatus status);

    // Used by getTemplatesByChannel()
    List<NotificationTemplate> findAllByChannelAndStatus(
            NotificationChannel channel, NotificationTemplateStatus status);
}
