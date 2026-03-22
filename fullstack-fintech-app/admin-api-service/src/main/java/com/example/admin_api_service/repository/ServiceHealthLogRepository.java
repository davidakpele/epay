package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.systemAndConfiguration.ServiceHealthLog;

@Repository
public interface ServiceHealthLogRepository extends JpaRepository<ServiceHealthLog, String> {

}