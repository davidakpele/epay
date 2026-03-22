package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.complianceAndRisk.AmlAlert;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, String> {

}