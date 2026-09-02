package com.epay.domain.developer.entity;

import com.epay.domain.developer.enums.ApiMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "developer_apps", indexes = {
        @Index(name = "idx_dev_app_owner",  columnList = "owner_user_id"),
        @Index(name = "idx_dev_app_name",   columnList = "app_name")
})
public class DeveloperApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dev_app_seq")
    @SequenceGenerator(name = "dev_app_seq", sequenceName = "dev_app_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    @Column(length = 500)
    private String description;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ApiMode mode = ApiMode.TEST;

    @Column(name = "live_approved", nullable = false)
    @Builder.Default
    private boolean liveApproved = false;

    @Column(name = "live_approved_by")
    private Long liveApprovedBy;

    @Column(name = "live_approved_at")
    private LocalDateTime liveApprovedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApiKey> apiKeys = new ArrayList<>();

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppWebhook> webhooks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
