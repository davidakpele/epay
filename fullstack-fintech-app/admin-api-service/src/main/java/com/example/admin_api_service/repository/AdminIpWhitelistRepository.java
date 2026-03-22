package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;

@Repository
public interface AdminIpWhitelistRepository extends JpaRepository<AdminIpWhitelist, String> {

}