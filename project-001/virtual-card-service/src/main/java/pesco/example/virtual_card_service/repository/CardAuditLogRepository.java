package pesco.example.virtual_card_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pesco.example.virtual_card_service.models.CardAuditLog;

@Repository
public interface CardAuditLogRepository extends JpaRepository<CardAuditLog, String> {

}
