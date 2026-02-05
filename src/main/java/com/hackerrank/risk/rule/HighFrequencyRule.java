package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class HighFrequencyRule implements RiskRule {
    // TODO: Implement high-frequency detection (e.g. > 5 transactions in 2-minute window)

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        return Optional.empty();
    }
}
