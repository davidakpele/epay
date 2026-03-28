package com.example.auth_user_service.interfaces;

import org.springframework.http.ResponseEntity;

import com.example.auth_user_service.payloads.DeleteteAccountRequest;

public interface IUserAccountCasesReportService {

    ResponseEntity<?> deleteUserAccount(Long id, DeleteteAccountRequest deleteAccountRequest);

}
