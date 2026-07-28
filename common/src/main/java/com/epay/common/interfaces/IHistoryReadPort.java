package com.epay.common.interfaces;

import com.epay.domain.history.dto.TransactionDTO;
import java.util.List;

public interface IHistoryReadPort {
    List<TransactionDTO> findRecentByUserId(Long userId, int limit);
}
