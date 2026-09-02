package com.epay.domain.auth.input;

import java.util.List;
import com.epay.domain.auth.dto.BankStatement;
import lombok.Data;

@Data
public class StatementRequest {

    private String email;
    private String username;
    private List<BankStatement> statements;
}

