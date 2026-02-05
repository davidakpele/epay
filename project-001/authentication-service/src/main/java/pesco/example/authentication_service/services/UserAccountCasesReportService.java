package pesco.example.authentication_service.services;

import org.springframework.http.ResponseEntity;

import pesco.example.authentication_service.payloads.DeleteteAccountRequest;

public interface UserAccountCasesReportService {

    ResponseEntity<?> deleteUserAccount(Long id, DeleteteAccountRequest deleteAccountRequest);

}
