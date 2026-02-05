package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.AccountHistory;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class MerchantDiversityRule implements RiskRule {
    private static final int MERCHANT_THRESHOLD = 3;
    private static final long TIME_WINDOW_MS = 300_000;

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        String accountId = transaction.getAccountId();
        AccountHistory history = store.getAccountHistory(accountId);
        
        if (history == null) {
            return Optional.empty();
        }
        
        long currentTime = transaction.getTimestamp();
        long windowStart = currentTime - TIME_WINDOW_MS;
        
        int uniqueMerchantCount = history.getUniqueMerchantCountInWindow(windowStart, currentTime);
        
        if (uniqueMerchantCount > MERCHANT_THRESHOLD) {
            return Optional.of(RiskReason.MERCHANT_DIVERSITY);
        }
        
        return Optional.empty();
    }
}
