package com.hackerrank.risk.storage;

import com.hackerrank.risk.model.Transaction;

import java.util.*;

public class AccountHistory {
    private final TreeMap<Long, Transaction> transactions;
    private final LinkedList<Double> recentAmounts;
    private double runningSum;
    private static final int AMOUNT_WINDOW_SIZE = 10;
    private final TreeMap<Long, String> merchantTimestamps;

    public AccountHistory() {
        this.transactions = new TreeMap<>();
        this.recentAmounts = new LinkedList<>();
        this.runningSum = 0.0;
        this.merchantTimestamps = new TreeMap<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.put(transaction.getTimestamp(), transaction);
        
        recentAmounts.add(transaction.getAmount());
        runningSum += transaction.getAmount();
        
        if (recentAmounts.size() > AMOUNT_WINDOW_SIZE) {
            double oldAmount = recentAmounts.removeFirst();
            runningSum -= oldAmount;
        }
        
        merchantTimestamps.put(transaction.getTimestamp(), transaction.getMerchantId());
    }

    public Collection<Transaction> getTransactionsInWindow(long fromTime, long toTime) {
        NavigableMap<Long, Transaction> window = transactions.subMap(fromTime, true, toTime, true);
        return window.values();
    }

    public int getTransactionCountInWindow(long fromTime, long toTime) {
        NavigableMap<Long, Transaction> window = transactions.subMap(fromTime, true, toTime, true);
        return window.size();
    }

    public double getRecentAverage() {
        if (recentAmounts.isEmpty()) {
            return 0.0;
        }
        return runningSum / recentAmounts.size();
    }

    public int getRecentTransactionCount() {
        return recentAmounts.size();
    }

    public Set<String> getUniqueMerchantsInWindow(long fromTime, long toTime) {
        NavigableMap<Long, String> window = merchantTimestamps.subMap(fromTime, true, toTime, true);
        return new HashSet<>(window.values());
    }

    public int getUniqueMerchantCountInWindow(long fromTime, long toTime) {
        return getUniqueMerchantsInWindow(fromTime, toTime).size();
    }

    public void cleanup(long cutoffTime) {
        transactions.headMap(cutoffTime, false).clear();
        merchantTimestamps.headMap(cutoffTime, false).clear();
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public Collection<Transaction> getAllTransactions() {
        return transactions.values();
    }
}
