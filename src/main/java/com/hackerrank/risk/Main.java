package com.hackerrank.risk;

import com.hackerrank.risk.engine.RiskEngine;
import com.hackerrank.risk.engine.RiskEngineImpl;
import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        RiskEngine engine = new RiskEngineImpl();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                if (isFirstLine && line.toLowerCase().contains("transaction")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                try {
                    Transaction transaction = parseTransaction(line);
                    
                    RiskResult result = engine.evaluateTransaction(transaction);
                    
                    System.out.println("Transaction " + transaction.getTransactionId() + ": " + result);
                    
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Transaction parseTransaction(String line) {
        String[] parts = line.split(",");
        
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid transaction format. Expected 5 fields, got " + parts.length);
        }
        
        String transactionId = parts[0].trim();
        String accountId = parts[1].trim();
        double amount = Double.parseDouble(parts[2].trim());
        long timestamp = Long.parseLong(parts[3].trim());
        String merchantId = parts[4].trim();
        
        return new Transaction(transactionId, accountId, amount, timestamp, merchantId);
    }
}
