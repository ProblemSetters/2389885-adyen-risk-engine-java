package com.hackerrank.risk.storage;

import com.hackerrank.risk.model.Transaction;

import java.util.HashMap;
import java.util.Map;

public class TransactionStore {
    private final Map<String, AccountHistory> accountHistories;

    public TransactionStore() {
        this.accountHistories = new HashMap<>();
    }

    public void storeTransaction(Transaction transaction) {
        String accountId = transaction.getAccountId();
        
        AccountHistory history = accountHistories.computeIfAbsent(
            accountId, 
            k -> new AccountHistory()
        );
        
        history.addTransaction(transaction);
    }

    public AccountHistory getAccountHistory(String accountId) {
        return accountHistories.get(accountId);
    }

    public boolean hasHistory(String accountId) {
        return accountHistories.containsKey(accountId);
    }

    public int getAccountCount() {
        return accountHistories.size();
    }

    public void cleanupOldTransactions(long cutoffTime) {
        for (AccountHistory history : accountHistories.values()) {
            history.cleanup(cutoffTime);
        }
    }

    public void clear() {
        accountHistories.clear();
    }
}
