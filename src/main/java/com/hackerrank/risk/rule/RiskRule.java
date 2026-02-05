package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public interface RiskRule {
    Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store);
}
