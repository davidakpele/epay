package com.epay.auth.interfaces;

import org.springframework.http.ResponseEntity;
import com.epay.domain.auth.input.DeleteAccountRequest;

public interface IUserAccountCasesReportService {

    ResponseEntity<?> deleteUserAccount(Long id, DeleteAccountRequest deleteAccountRequest);

}