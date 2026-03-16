package com.example.admin_api_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/admin")
public class BusinessApiController {

    @PreAuthorize("hasRole('ADMIN') and @security.isOwner(#userId)")
    @GetMapping("/{userId}/wallet")
    public ResponseEntity<?> getWallet(
            @PathVariable Long userId, 
            HttpServletResponse response,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok("Hello welcome back");
    }

}