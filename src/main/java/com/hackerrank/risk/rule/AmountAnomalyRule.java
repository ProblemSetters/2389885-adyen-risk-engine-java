package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.AccountHistory;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class AmountAnomalyRule implements RiskRule {
    private static final double MULTIPLIER = 3.0;

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        String accountId = transaction.getAccountId();
        AccountHistory history = store.getAccountHistory(accountId);

        if (history == null || history.getRecentTransactionCount() == 0) {
            return Optional.empty();
        }

        double averageAmount = history.getRecentAverage();
        double threshold = averageAmount * MULTIPLIER;
        
        if (transaction.getAmount() > threshold) {
            return Optional.of(RiskReason.AMOUNT_ANOMALY);
        }
        
        return Optional.empty();
    }
}
