package com.epay.domain.blacklist.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklist_entries", indexes = {
        @Index(name = "idx_bl_type_value", columnList = "type, value"),
        @Index(name = "idx_bl_user_id",    columnList = "user_id"),
        @Index(name = "idx_bl_active",     columnList = "active")
})
public class BlacklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "user_id")
    private Long userId;

    private Long WalletId;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 500)
    private String reason;

    @Column(name = "added_by")
    private String addedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
