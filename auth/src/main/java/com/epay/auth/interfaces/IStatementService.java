package com.epay.auth.interfaces;

import java.io.IOException;
import java.util.List;
import com.epay.domain.auth.dto.BankStatement;

public interface IStatementService {
    byte[] generateBankStatementPDF(List<BankStatement> statements) throws IOException;
    void generateAndSendBankStatement(String email, String username, List<BankStatement> statements);
}