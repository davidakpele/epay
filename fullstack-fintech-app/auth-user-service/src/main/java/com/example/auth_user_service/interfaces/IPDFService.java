package com.example.auth_user_service.interfaces;

import java.util.List;
import com.example.auth_user_service.dtos.BankStatement;

public interface IPDFService {

    byte[] generateBankStatementPDF(List<BankStatement> statements);
    void generateAndSendBankStatement(String email, String username, List<BankStatement> statements);


}
