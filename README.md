# Real-Time Transaction Risk Engine

**Candidate task:** The risk engine is stubbed so that every transaction is reported as RISKY (placeholder reason). The three risk rules are also stubbed. The project builds and runs, but **all 17 tests fail** and the output is wrong. Implement the three rules (`AmountAnomalyRule`, `HighFrequencyRule`, `MerchantDiversityRule`) and fix `RiskEngineImpl` to use them so that all tests pass.

---

## Overview

Build an in-memory risk engine in **core Java only** (no frameworks). For each transaction, evaluate it as **SAFE** or **RISKY** using that account’s recent history. Return status and, when risky, one or more reasons.

**Transaction model:** `transactionId`, `accountId`, `amount`, `timestamp` (epoch ms), `merchantId`.

---

## Rules

1. **High frequency** — RISKY if more than 5 transactions for the same account in the last 2 minutes.
2. **Amount anomaly** — RISKY if amount is greater than 3× the average of the account’s last 10 transactions.
3. **Merchant diversity** — RISKY if more than 3 unique merchants for the same account in the last 5 minutes.

Reasons: `HIGH_FREQUENCY`, `AMOUNT_ANOMALY`, `MERCHANT_DIVERSITY`.

**When more than one rule triggers:** Include **all** triggered reasons in the result. A transaction can be RISKY for multiple reasons (e.g. high frequency and amount anomaly); the output must list every reason that applied. Tests verify that multi-reason outcomes are correct.

---

## Sample data: sample-transactions.csv

- **31 transactions**, accounts A001–A007. Use this file to run the app and see output in the terminal.
- **Format:** `transactionId,accountId,amount,timestamp,merchantId` (first line is header).
- **Intended outcomes:** A001/A005 normal → SAFE; A002 → T009, T019 RISKY (high frequency); A003 → T013 RISKY (amount anomaly); A004 → T017 RISKY (merchant diversity); A006 → T025 RISKY (amount anomaly); **A007 → T031 RISKY (multiple reasons: HIGH_FREQUENCY, AMOUNT_ANOMALY, MERCHANT_DIVERSITY)**.
- **Large dataset:** `target/generated-transactions.csv` (15k rows) is created only when you run tests; use the sample file for manual runs.


Intended Output

Transaction T001: SAFE
Transaction T002: SAFE
Transaction T003: SAFE
Transaction T004: SAFE
Transaction T005: SAFE
Transaction T006: SAFE
Transaction T007: SAFE
Transaction T008: SAFE
Transaction T009: RISKY [HIGH_FREQUENCY]
Transaction T010: SAFE
Transaction T011: SAFE
Transaction T012: SAFE
Transaction T013: RISKY [AMOUNT_ANOMALY]
Transaction T014: SAFE
Transaction T015: SAFE
Transaction T016: SAFE
Transaction T017: RISKY [MERCHANT_DIVERSITY]
Transaction T018: SAFE
Transaction T019: RISKY [HIGH_FREQUENCY]
Transaction T020: SAFE
Transaction T021: SAFE
Transaction T022: SAFE
Transaction T023: SAFE
Transaction T024: SAFE
Transaction T025: RISKY [AMOUNT_ANOMALY]
Transaction T026: SAFE
Transaction T027: SAFE
Transaction T028: SAFE
Transaction T029: SAFE
Transaction T030: SAFE
Transaction T031: RISKY [HIGH_FREQUENCY, AMOUNT_ANOMALY, MERCHANT_DIVERSITY]
---



## Terminal output and commands

**Build**
```bash
mvn clean install -DskipTests
```

**Run the app (see results in terminal)**
```bash
mvn clean package && java -jar target/risk-engine-1.0.jar < sample-transactions.csv
```

**Output format** (one line per transaction)
- SAFE: `Transaction <id>: SAFE`
- RISKY (one reason): `Transaction <id>: RISKY [HIGH_FREQUENCY]`
- RISKY (multiple reasons): `Transaction <id>: RISKY [HIGH_FREQUENCY, AMOUNT_ANOMALY]` — list all reasons that triggered, in any order.

**Example**
```
Transaction T001: SAFE
Transaction T009: RISKY [HIGH_FREQUENCY]
Transaction T013: RISKY [AMOUNT_ANOMALY]
```

**Run tests**
```bash
mvn clean test
```

---

## Performance and optimization

- Evaluate each transaction using **only that account’s history**; avoid full rescans.
- Use data structures that support **time-window queries** and **incremental averages** (e.g. ordered by timestamp, bounded windows).
- Tests expect: 10k transactions in &lt; 2s; single-account 10k in &lt; 3s; scaling better than linear in history size. Old data should be removable to keep memory bounded.

**Success:** App run on `sample-transactions.csv` shows expected SAFE/RISKY lines, and `mvn clean test` passes.
