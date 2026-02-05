package com.hackerrank.risk;

import com.hackerrank.risk.engine.RiskEngine;
import com.hackerrank.risk.engine.RiskEngineImpl;
import com.hackerrank.risk.generator.TransactionDataGenerator;
import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.RiskStatus;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.AccountHistory;
import com.hackerrank.risk.storage.TransactionStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests to verify the risk engine can handle large volumes efficiently.
 * Generates the large dataset (15k rows) when tests run.
 */
public class PerformanceTest {

    private static void section(String title) {
        System.out.println();
        System.out.println("---------- " + title + " ----------");
    }

    @BeforeAll
    static void generateLargeDataset() throws IOException {
        String path = "target/generated-transactions.csv";
        Files.createDirectories(Paths.get("target"));
        TransactionDataGenerator.generateTransactions(path);
    }

    @Test
    @DisplayName("Test Case 8: 10K Transactions Performance - Should complete in < 2 seconds")
    public void test10KTransactionsPerformance() {
        section("Test Case 8: 10K Transactions Performance");
        RiskEngine engine = new RiskEngineImpl();
        List<Transaction> transactions = generateRandomTransactions(10000, 100);
        
        long startTime = System.currentTimeMillis();
        
        int riskyCount = 0;
        for (Transaction transaction : transactions) {
            RiskResult result = engine.evaluateTransaction(transaction);
            if (result.getStatus().name().equals("RISKY")) {
                riskyCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("  Processed 10,000 transactions in " + duration + "ms");
        System.out.println("  Risky transactions detected: " + riskyCount);
        System.out.println("  Average time per transaction: " + (duration / 10000.0) + "ms");
        
        // Performance assertion: should complete in less than 2 seconds
        assertTrue(duration < 2000, 
                  "Processing 10K transactions took " + duration + "ms, expected < 2000ms");
        
        // Sanity check: should have processed all transactions
        assertTrue(riskyCount >= 0, "Should have evaluated all transactions");
        // With correct implementation, not all transactions are risky (expect a mix of SAFE and RISKY)
        assertTrue(riskyCount < transactions.size(),
                "Expected mix of SAFE and RISKY outcomes, not all RISKY");
    }

    @Test
    @DisplayName("Test Case 9: Memory Cleanup - Old transactions should be cleanable")
    public void testMemoryCleanup() {
        section("Test Case 9: Memory Cleanup");
        // First transaction for an account should be SAFE (no history yet)
        RiskEngine engine = new RiskEngineImpl();
        Transaction first = new Transaction("T0", "A0", 100.0, 1000000000L, "M0");
        assertEquals(RiskStatus.SAFE, engine.evaluateTransaction(first).getStatus(),
                "First transaction for an account should be SAFE");
        TransactionStore store = new TransactionStore();
        String accountId = "A001";
        long baseTime = 1000000000L;
        
        // Create transactions spread over 1 hour (3,600,000 ms)
        int transactionCount = 1000;
        long timeIncrement = 3600; // 3.6 seconds between transactions = 1 hour total
        
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = new Transaction(
                "T" + String.format("%04d", i),
                accountId,
                100.0 + (i % 50),
                baseTime + (i * timeIncrement),
                "M" + (i % 10)
            );
            store.storeTransaction(t);
        }
        
        AccountHistory history = store.getAccountHistory(accountId);
        assertNotNull(history);
        
        // Before cleanup, should have all transactions
        int countBefore = history.getTransactionCount();
        assertEquals(transactionCount, countBefore, "Should have all 1000 transactions");
        
        // Cleanup transactions older than last 10 minutes (600,000 ms)
        long cutoffTime = baseTime + (transactionCount * timeIncrement) - 600000;
        history.cleanup(cutoffTime);
        
        int countAfter = history.getTransactionCount();
        
        System.out.println("  Transactions before cleanup: " + countBefore);
        System.out.println("  Transactions after cleanup: " + countAfter);
        System.out.println("  Transactions removed: " + (countBefore - countAfter));
        
        // Should have removed old transactions
        assertTrue(countAfter < countBefore, "Cleanup should remove old transactions");
        
        // Should retain recent transactions (approximately last 10 minutes worth)
        // With 3.6s increment, 10 minutes = 600s = ~167 transactions
        assertTrue(countAfter < 200, "Should have bounded memory after cleanup");
    }

    @Test
    @DisplayName("Test Case 10: Worst Case - 10K transactions for single account")
    public void testWorstCaseSingleAccount() {
        section("Test Case 10: Worst Case (Single Account)");
        RiskEngine engine = new RiskEngineImpl();
        String accountId = "A001";
        long baseTime = 1000000000L;
        
        // All 10,000 transactions for the same account
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            transactions.add(new Transaction(
                "T" + String.format("%05d", i),
                accountId,
                100.0 + (i % 100),
                baseTime + (i * 1000), // 1 second apart
                "M" + (i % 20)
            ));
        }
        
        long startTime = System.currentTimeMillis();
        
        int riskyCount = 0;
        for (Transaction transaction : transactions) {
            RiskResult result = engine.evaluateTransaction(transaction);
            if (result.getStatus().name().equals("RISKY")) {
                riskyCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("  Processed 10,000 transactions in " + duration + "ms");
        System.out.println("  Risky transactions detected: " + riskyCount);
        
        // Should complete in less than 3 seconds even for worst case
        assertTrue(duration < 3000, 
                  "Worst case processing took " + duration + "ms, expected < 3000ms");
        
        // Should have detected some risky transactions due to high frequency
        assertTrue(riskyCount > 0, "Should detect risky transactions in high-frequency scenario");
        // With correct implementation, not all are risky (early transactions should be SAFE)
        assertTrue(riskyCount < transactions.size(),
                "Expected mix of SAFE and RISKY outcomes, not all RISKY");
    }

    @Test
    @DisplayName("Performance: Verify O(log n) complexity with TreeMap operations")
    public void testTreeMapPerformance() {
        section("TreeMap Performance (O(log n) scaling)");
        // First transaction for an account should be SAFE (no history yet)
        RiskEngine engine = new RiskEngineImpl();
        Transaction first = new Transaction("T0", "A0", 100.0, 1000000000L, "M0");
        assertEquals(RiskStatus.SAFE, engine.evaluateTransaction(first).getStatus(),
                "First transaction for an account should be SAFE");
        String accountId = "A001";
        long baseTime = 1000000000L;
        
        // Test with increasing dataset sizes
        int[] sizes = {100, 500, 1000, 5000};
        long[] durations = new long[sizes.length];
        
        for (int i = 0; i < sizes.length; i++) {
            RiskEngine freshEngine = new RiskEngineImpl();
            int size = sizes[i];
            
            long start = System.currentTimeMillis();
            
            // Build up history and measure last transaction evaluation
            for (int j = 0; j < size - 1; j++) {
                freshEngine.evaluateTransaction(new Transaction(
                    "T" + j, accountId, 100.0, baseTime + (j * 1000), "M001"
                ));
            }
            
            // Measure evaluation with full history
            long evalStart = System.nanoTime();
            freshEngine.evaluateTransaction(new Transaction(
                "T" + size, accountId, 100.0, baseTime + (size * 1000), "M001"
            ));
            long evalEnd = System.nanoTime();
            
            durations[i] = evalEnd - evalStart;
            
            System.out.println("  Size " + size + ": " + (durations[i] / 1000) + " microseconds");
        }
        System.out.println("  Time growth ratio (5000/100): " + ((double) durations[3] / durations[0]));
        
        // Verify logarithmic growth: duration shouldn't grow linearly with size
        // Even with 50x more data (100->5000), time should be much less than 50x
        double growthRatio = (double) durations[3] / durations[0];
        assertTrue(growthRatio < 20, "Performance should scale logarithmically, not linearly");
    }

    @Test
    @DisplayName("Stress Test: Multiple accounts with varied patterns")
    public void testMultipleAccountsStress() {
        section("Stress Test: Multiple Accounts");
        // First transaction for an account should be SAFE (no history yet)
        RiskEngine engine = new RiskEngineImpl();
        Transaction first = new Transaction("T0", "A0", 100.0, 1000000000L, "M0");
        assertEquals(RiskStatus.SAFE, engine.evaluateTransaction(first).getStatus(),
                "First transaction for an account should be SAFE");
        long baseTime = 1000000000L;
        
        // Create varied patterns across 100 accounts
        int accountCount = 100;
        int transactionsPerAccount = 100;
        
        long startTime = System.currentTimeMillis();
        
        for (int accountIdx = 0; accountIdx < accountCount; accountIdx++) {
            String accountId = "A" + String.format("%03d", accountIdx);
            
            for (int txIdx = 0; txIdx < transactionsPerAccount; txIdx++) {
                Transaction t = new Transaction(
                    "T" + accountIdx + "_" + txIdx,
                    accountId,
                    50.0 + (txIdx * 5),
                    baseTime + (txIdx * 2000) + (accountIdx * 100),
                    "M" + (txIdx % 15)
                );
                engine.evaluateTransaction(t);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("  Processed " + (accountCount * transactionsPerAccount) +
                         " transactions across " + accountCount + " accounts in " + duration + "ms");
        
        assertTrue(duration < 2000, "Multi-account stress test should complete in < 2s");
    }

    @Test
    @DisplayName("Correctness: High volume should still detect risks accurately")
    public void testHighVolumeAccuracy() {
        section("High Volume Accuracy");
        RiskEngine engine = new RiskEngineImpl();
        String accountId = "A001";
        long baseTime = 1000000000L;
        
        // Create a known risky scenario embedded in high volume
        // First, create normal transactions
        for (int i = 0; i < 100; i++) {
            engine.evaluateTransaction(new Transaction(
                "T" + i, accountId, 100.0, baseTime + (i * 5000), "M001"
            ));
        }
        
        // Now create high-frequency burst (6 transactions in 1 minute)
        long burstTime = baseTime + 600000;
        int riskyDetected = 0;
        
        for (int i = 0; i < 6; i++) {
            Transaction t = new Transaction(
                "BURST" + i, accountId, 100.0, burstTime + (i * 5000), "M001"
            );
            RiskResult result = engine.evaluateTransaction(t);
            if (result.getStatus().name().equals("RISKY")) {
                riskyDetected++;
            }
        }
        
        System.out.println("  Risky transactions in burst: " + riskyDetected + " / 6");
        assertTrue(riskyDetected > 0, "Should detect high-frequency pattern even in high volume");
        // With correct implementation, not all 6 in the burst are risky (e.g. first few may be SAFE)
        assertTrue(riskyDetected < 6,
                "Expected mix of SAFE and RISKY in burst, not all RISKY");
    }

    /**
     * Helper method to generate random transactions for performance testing.
     */
    private List<Transaction> generateRandomTransactions(int count, int accountCount) {
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducibility
        long baseTime = 1000000000L;
        
        String[] merchants = new String[50];
        for (int i = 0; i < merchants.length; i++) {
            merchants[i] = "M" + String.format("%03d", i);
        }
        
        for (int i = 0; i < count; i++) {
            String txId = "T" + String.format("%06d", i);
            String accountId = "A" + String.format("%04d", random.nextInt(accountCount));
            double amount = 50.0 + (random.nextDouble() * 450.0); // $50-$500
            long timestamp = baseTime + (i * 100) + random.nextInt(1000);
            String merchantId = merchants[random.nextInt(merchants.length)];
            
            transactions.add(new Transaction(txId, accountId, amount, timestamp, merchantId));
        }
        
        return transactions;
    }
}
