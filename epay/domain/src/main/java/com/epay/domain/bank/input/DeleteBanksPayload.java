package com.epay.domain.bank.input;

import lombok.Data;
 
import java.util.List;
 
@Data
public class DeleteBanksPayload {
    private List<Long> ids;
}