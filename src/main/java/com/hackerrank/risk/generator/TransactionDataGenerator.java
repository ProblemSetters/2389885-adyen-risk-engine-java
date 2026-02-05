package com.hackerrank.risk.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TransactionDataGenerator {
    private static final int TOTAL_TRANSACTIONS = 15000;
    private static final int ACCOUNT_COUNT = 100;
    private static final int MERCHANT_COUNT = 50;
    private static final long BASE_TIMESTAMP = 1000000000L;
    private static final Random random = new Random(42);

    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "sample-transactions.csv";
        try {
            generateTransactions(filename);
        } catch (IOException e) {
            System.err.println("Error generating transactions: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void generateTransactions(String filename) throws IOException {
        System.out.println("Generating 15000 transactions for testing...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("transactionId,accountId,amount,timestamp,merchantId");
            writer.newLine();

            long currentTime = BASE_TIMESTAMP;

            double[] accountAverages = new double[ACCOUNT_COUNT];
            for (int i = 0; i < ACCOUNT_COUNT; i++) {
                accountAverages[i] = 100.0 + random.nextDouble() * 200.0;
            }

            for (int i = 0; i < TOTAL_TRANSACTIONS; i++) {
                String transactionId = String.format("T%05d", i + 1);

                int accountIdx;
                if (random.nextDouble() < 0.8) {
                    accountIdx = random.nextInt(20);
                } else {
                    accountIdx = random.nextInt(ACCOUNT_COUNT);
                }
                String accountId = String.format("A%03d", accountIdx + 1);

                double amount;
                double baseAmount = accountAverages[accountIdx];

                if (random.nextDouble() < 0.05) {
                    amount = baseAmount * (4.0 + random.nextDouble() * 2.0);
                } else if (random.nextDouble() < 0.10) {
                    amount = baseAmount * (2.0 + random.nextDouble());
                } else {
                    amount = baseAmount * (0.5 + random.nextDouble());
                }
                amount = Math.round(amount * 100.0) / 100.0;

                if (random.nextDouble() < 0.7) {
                    currentTime += 1000 + random.nextInt(29000);
                } else {
                    currentTime += 30000 + random.nextInt(270000);
                }

                int merchantIdx;
                if (accountIdx < 10 && random.nextDouble() < 0.3) {
                    merchantIdx = random.nextInt(MERCHANT_COUNT);
                } else {
                    merchantIdx = (accountIdx * 3 + random.nextInt(5)) % MERCHANT_COUNT;
                }
                String merchantId = String.format("M%03d", merchantIdx + 1);

                writer.write(String.format("%s,%s,%.2f,%d,%s",
                    transactionId, accountId, amount, currentTime, merchantId));
                writer.newLine();

                accountAverages[accountIdx] = accountAverages[accountIdx] * 0.95 + amount * 0.05;
            }
        }
    }

    public static void generateRiskyScenario(BufferedWriter writer, String accountPrefix, long startTime, int startId) throws IOException {
        for (int i = 0; i < 10; i++) {
            String txId = String.format("T%05d", startId + i);
            long timestamp = startTime + (i * 5000);
            writer.write(String.format("%s,%s,%.2f,%d,%s%n",
                txId, accountPrefix, 100.0, timestamp, "M001"));
        }
    }
}
