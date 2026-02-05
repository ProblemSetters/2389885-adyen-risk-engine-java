package com.hackerrank.risk.model;

import java.util.Objects;

public class Transaction implements Comparable<Transaction> {
    private final String transactionId;
    private final String accountId;
    private final double amount;
    private final long timestamp;
    private final String merchantId;

    public Transaction(String transactionId, String accountId, double amount, long timestamp, String merchantId) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.merchantId = merchantId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMerchantId() {
        return merchantId;
    }

    @Override
    public int compareTo(Transaction other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}
