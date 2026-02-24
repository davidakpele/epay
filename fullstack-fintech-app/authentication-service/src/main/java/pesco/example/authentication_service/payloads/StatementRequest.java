package pesco.example.authentication_service.payloads;

import java.util.List;
import pesco.example.authentication_service.dtos.BankStatement;

public class StatementRequest {

    private String email;
    private List<BankStatement> statements;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<BankStatement> getStatements() {
        return statements;
    }

    public void setStatements(List<BankStatement> statements) {
        this.statements = statements;
    }
}
