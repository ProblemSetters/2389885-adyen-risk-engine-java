package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class AmountAnomalyRule implements RiskRule {
    // TODO: Implement amount anomaly detection (e.g. transaction amount > 3x recent average)

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        return Optional.empty();
    }
}
