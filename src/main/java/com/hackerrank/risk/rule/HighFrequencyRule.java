package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.AccountHistory;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class HighFrequencyRule implements RiskRule {
    private static final int TRANSACTION_THRESHOLD = 5;
    private static final long TIME_WINDOW_MS = 120_000;

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        String accountId = transaction.getAccountId();
        AccountHistory history = store.getAccountHistory(accountId);
        
        if (history == null) {
            return Optional.empty();
        }
        
        long currentTime = transaction.getTimestamp();
        long windowStart = currentTime - TIME_WINDOW_MS;
        
        int transactionCount = history.getTransactionCountInWindow(windowStart, currentTime);
        
        if (transactionCount > TRANSACTION_THRESHOLD) {
            return Optional.of(RiskReason.HIGH_FREQUENCY);
        }
        
        return Optional.empty();
    }
}
