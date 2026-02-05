package com.hackerrank.risk;

import com.hackerrank.risk.engine.RiskEngine;
import com.hackerrank.risk.engine.RiskEngineImpl;
import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.RiskStatus;
import com.hackerrank.risk.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Risk Engine.
 * Tests all risk detection rules and edge cases.
 */
public class RiskEngineTest {
    private RiskEngine engine;
    private long baseTime;

    @BeforeEach
    public void setUp() {
        engine = new RiskEngineImpl();
        baseTime = 1000000000L; // Use fixed base time for predictable tests
    }

    @Test
    @DisplayName("Test Case 1: Single Transaction - Should be SAFE")
    public void testSingleTransaction() {
        Transaction t1 = new Transaction("T001", "A001", 100.0, baseTime, "M001");
        RiskResult result = engine.evaluateTransaction(t1);
        
        assertEquals(RiskStatus.SAFE, result.getStatus());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    @DisplayName("Test Case 2: High Frequency Detection - 6 transactions in 1 minute")
    public void testHighFrequencyDetection() {
        String accountId = "A001";
        
        // Send 5 transactions - should all be SAFE
        for (int i = 0; i < 5; i++) {
            Transaction t = new Transaction("T00" + (i + 1), accountId, 100.0, 
                                          baseTime + (i * 10000), "M001");
            RiskResult result = engine.evaluateTransaction(t);
            assertEquals(RiskStatus.SAFE, result.getStatus(), 
                        "Transaction " + (i + 1) + " should be SAFE");
        }
        
        // 6th transaction within 2 minutes should trigger HIGH_FREQUENCY
        Transaction t6 = new Transaction("T006", accountId, 100.0, baseTime + 50000, "M001");
        RiskResult result = engine.evaluateTransaction(t6);
        
        assertEquals(RiskStatus.RISKY, result.getStatus());
        assertTrue(result.getReasons().contains(RiskReason.HIGH_FREQUENCY));
    }

    @Test
    @DisplayName("Test Case 3: Amount Anomaly Detection - Transaction 5x average")
    public void testAmountAnomalyDetection() {
        String accountId = "A002";
        
        // Send 10 transactions at $100 each
        for (int i = 0; i < 10; i++) {
            Transaction t = new Transaction("T" + String.format("%03d", i + 1), accountId, 100.0, 
                                          baseTime + (i * 10000), "M001");
            engine.evaluateTransaction(t);
        }
        
        // Transaction at $500 (5x average) should trigger AMOUNT_ANOMALY
        Transaction anomaly = new Transaction("T011", accountId, 500.0, baseTime + 100000, "M001");
        RiskResult result = engine.evaluateTransaction(anomaly);
        
        assertEquals(RiskStatus.RISKY, result.getStatus());
        assertTrue(result.getReasons().contains(RiskReason.AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Test Case 4: Merchant Diversity - 4 different merchants in 3 minutes")
    public void testMerchantDiversityDetection() {
        String accountId = "A003";
        
        // First 3 merchants - should be SAFE
        Transaction t1 = new Transaction("T001", accountId, 100.0, baseTime, "M001");
        Transaction t2 = new Transaction("T002", accountId, 100.0, baseTime + 30000, "M002");
        Transaction t3 = new Transaction("T003", accountId, 100.0, baseTime + 60000, "M003");
        
        engine.evaluateTransaction(t1);
        engine.evaluateTransaction(t2);
        RiskResult r3 = engine.evaluateTransaction(t3);
        assertEquals(RiskStatus.SAFE, r3.getStatus());
        
        // 4th merchant within 5 minutes should trigger MERCHANT_DIVERSITY
        Transaction t4 = new Transaction("T004", accountId, 100.0, baseTime + 90000, "M004");
        RiskResult result = engine.evaluateTransaction(t4);
        
        assertEquals(RiskStatus.RISKY, result.getStatus());
        assertTrue(result.getReasons().contains(RiskReason.MERCHANT_DIVERSITY));
    }

    @Test
    @DisplayName("Test Case 5: Sliding Window Validation - Old transactions don't count")
    public void testSlidingWindowValidation() {
        String accountId = "A004";
        
        // 3 transactions at t=0
        engine.evaluateTransaction(new Transaction("T001", accountId, 100.0, baseTime, "M001"));
        engine.evaluateTransaction(new Transaction("T002", accountId, 100.0, baseTime + 10000, "M001"));
        engine.evaluateTransaction(new Transaction("T003", accountId, 100.0, baseTime + 20000, "M001"));
        
        // 3 more transactions at t=130s (outside 2-minute window for high frequency)
        long laterTime = baseTime + 130000; // 130 seconds later
        
        Transaction t4 = new Transaction("T004", accountId, 100.0, laterTime, "M001");
        Transaction t5 = new Transaction("T005", accountId, 100.0, laterTime + 10000, "M001");
        Transaction t6 = new Transaction("T006", accountId, 100.0, laterTime + 20000, "M001");
        
        RiskResult r4 = engine.evaluateTransaction(t4);
        RiskResult r5 = engine.evaluateTransaction(t5);
        RiskResult r6 = engine.evaluateTransaction(t6);
        
        // All should be SAFE because old transactions are outside the window
        assertEquals(RiskStatus.SAFE, r4.getStatus());
        assertEquals(RiskStatus.SAFE, r5.getStatus());
        assertEquals(RiskStatus.SAFE, r6.getStatus());
    }

    @Test
    @DisplayName("Test Case 6: Multiple Rules Triggered Simultaneously")
    public void testMultipleRulesTriggered() {
        String accountId = "A005";
        
        // Build up history: 5 transactions at $100 each with different merchants
        engine.evaluateTransaction(new Transaction("T001", accountId, 100.0, baseTime, "M001"));
        engine.evaluateTransaction(new Transaction("T002", accountId, 100.0, baseTime + 10000, "M002"));
        engine.evaluateTransaction(new Transaction("T003", accountId, 100.0, baseTime + 20000, "M003"));
        engine.evaluateTransaction(new Transaction("T004", accountId, 100.0, baseTime + 30000, "M001"));
        engine.evaluateTransaction(new Transaction("T005", accountId, 100.0, baseTime + 40000, "M002"));
        
        // 6th transaction: high amount + different merchant + high frequency
        // This should trigger both HIGH_FREQUENCY and AMOUNT_ANOMALY and MERCHANT_DIVERSITY
        Transaction risky = new Transaction("T006", accountId, 500.0, baseTime + 50000, "M004");
        RiskResult result = engine.evaluateTransaction(risky);
        
        assertEquals(RiskStatus.RISKY, result.getStatus());
        assertTrue(result.getReasons().size() >= 2, "Should trigger multiple rules");
        assertTrue(result.getReasons().contains(RiskReason.HIGH_FREQUENCY));
        assertTrue(result.getReasons().contains(RiskReason.AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Test Case 7: Multiple Accounts - Independent Evaluation")
    public void testMultipleAccountsIndependence() {
        // Account A001: Build up high frequency
        for (int i = 0; i < 6; i++) {
            engine.evaluateTransaction(new Transaction("TA" + i, "A001", 100.0, 
                                                      baseTime + (i * 10000), "M001"));
        }
        
        // Account A002: Single transaction should be SAFE
        Transaction t2 = new Transaction("TB1", "A002", 100.0, baseTime + 60000, "M002");
        RiskResult result2 = engine.evaluateTransaction(t2);
        assertEquals(RiskStatus.SAFE, result2.getStatus(), "A002 should be independent");
        
        // Account A003: Build merchant diversity
        engine.evaluateTransaction(new Transaction("TC1", "A003", 100.0, baseTime, "M001"));
        engine.evaluateTransaction(new Transaction("TC2", "A003", 100.0, baseTime + 10000, "M002"));
        engine.evaluateTransaction(new Transaction("TC3", "A003", 100.0, baseTime + 20000, "M003"));
        
        // Each account should be evaluated independently
        Transaction t3 = new Transaction("TC4", "A003", 100.0, baseTime + 30000, "M004");
        RiskResult result3 = engine.evaluateTransaction(t3);
        assertEquals(RiskStatus.RISKY, result3.getStatus());
        assertTrue(result3.getReasons().contains(RiskReason.MERCHANT_DIVERSITY));
    }

    @Test
    @DisplayName("Edge Case: Exactly at threshold should be SAFE")
    public void testExactlyAtThreshold() {
        String accountId = "A006";
        
        // Exactly 5 transactions (threshold is > 5)
        for (int i = 0; i < 5; i++) {
            Transaction t = new Transaction("T" + i, accountId, 100.0, 
                                          baseTime + (i * 10000), "M001");
            RiskResult result = engine.evaluateTransaction(t);
            assertEquals(RiskStatus.SAFE, result.getStatus());
        }
    }

    @Test
    @DisplayName("Edge Case: Amount exactly at 3x average should be SAFE")
    public void testAmountExactlyAtThreshold() {
        String accountId = "A007";
        
        // 5 transactions at $100, spaced out to avoid high frequency trigger
        for (int i = 0; i < 5; i++) {
            engine.evaluateTransaction(new Transaction("T" + i, accountId, 100.0, 
                                                      baseTime + (i * 30000), "M001")); // 30s apart
        }
        
        // Transaction at exactly 3x (300) should be SAFE (threshold is >3x)
        // Place it 2+ minutes after the first to avoid high frequency
        Transaction t = new Transaction("T5", accountId, 300.0, baseTime + 150000, "M001");
        RiskResult result = engine.evaluateTransaction(t);
        
        // Should be SAFE because 300 is not > 300, and no other rules triggered
        assertEquals(RiskStatus.SAFE, result.getStatus());
        assertFalse(result.getReasons().contains(RiskReason.AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Edge Case: Just above 3x average should be RISKY")
    public void testAmountJustAboveThreshold() {
        String accountId = "A008";
        
        // 5 transactions at $100, spaced out to avoid high frequency trigger
        for (int i = 0; i < 5; i++) {
            engine.evaluateTransaction(new Transaction("T" + i, accountId, 100.0, 
                                                      baseTime + (i * 30000), "M001")); // 30s apart
        }
        
        // Transaction just above 3x (301) should be RISKY
        // Place it 2+ minutes after the first to avoid high frequency
        Transaction t = new Transaction("T5", accountId, 301.0, baseTime + 150000, "M001");
        RiskResult result = engine.evaluateTransaction(t);
        
        assertEquals(RiskStatus.RISKY, result.getStatus());
        assertTrue(result.getReasons().contains(RiskReason.AMOUNT_ANOMALY));
    }

    @Test
    @DisplayName("Edge Case: Same merchant multiple times doesn't increase diversity")
    public void testSameMerchantMultipleTimes() {
        String accountId = "A009";
        
        // 10 transactions with same merchant
        for (int i = 0; i < 10; i++) {
            Transaction t = new Transaction("T" + i, accountId, 100.0, 
                                          baseTime + (i * 10000), "M001");
            RiskResult result = engine.evaluateTransaction(t);
            
            // Should never trigger merchant diversity with only 1 unique merchant
            assertFalse(result.getReasons().contains(RiskReason.MERCHANT_DIVERSITY));
        }
    }
}
