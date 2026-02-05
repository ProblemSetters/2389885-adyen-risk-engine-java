# Transaction Risk Engine

Build an in-memory risk engine in **core Java** that marks each transaction as **SAFE** or **RISKY** using that account’s recent history. The engine and rules are currently placeholders (every transaction is marked RISKY); implement the rules and wire them in `RiskEngineImpl` so all tests pass.

---

## Risk rules

| Rule | Condition |
|------|-----------|
| **HIGH_FREQUENCY** | More than 5 transactions in the last 2 minutes |
| **AMOUNT_ANOMALY** | Amount &gt; 3× the average of the account’s last 10 transactions |
| **MERCHANT_DIVERSITY** | More than 3 unique merchants in the last 5 minutes |

A transaction can trigger multiple rules; return **all** triggered reasons. Reasons may appear in any order.

---

## Input / output

**Input:** CSV with header `transactionId,accountId,amount,timestamp,merchantId` (one transaction per line).

**Output:** One line per transaction.
- SAFE: `Transaction <id>: SAFE`
- RISKY: `Transaction <id>: RISKY [REASON1, REASON2]`

---

## Run commands

**Build**
```bash
mvn clean install -DskipTests
```

**Run with sample data**
```bash
mvn clean package -DskipTests && java -jar target/risk-engine-1.0.jar < sample-transactions.csv
```

**Run tests**
```bash
mvn clean test
```

---

## sample-transactions.csv and verification

- **File:** `sample-transactions.csv` — 31 transactions across accounts A001–A007.
- **Use it for:** Manual runs and checking output. Pipe it into the app (see command above).
- **Verify:** Run the app and compare your output to the expected lines below. Key cases: T009 and T019 → `RISKY [HIGH_FREQUENCY]`; T013 and T025 → `RISKY [AMOUNT_ANOMALY]`; T017 → `RISKY [MERCHANT_DIVERSITY]`; **T031** → `RISKY [HIGH_FREQUENCY, AMOUNT_ANOMALY, MERCHANT_DIVERSITY]` (multiple reasons). All other lines should be `SAFE`.

**Expected output (excerpt)**
```
Transaction T001: SAFE
Transaction T009: RISKY [HIGH_FREQUENCY]
Transaction T013: RISKY [AMOUNT_ANOMALY]
Transaction T017: RISKY [MERCHANT_DIVERSITY]
Transaction T031: RISKY [HIGH_FREQUENCY, AMOUNT_ANOMALY, MERCHANT_DIVERSITY]
```

---

## Flow overview

See `flow-overview.txt` for a Mermaid diagram of the application flow (Input → Main → RiskEngineImpl → Output, with TransactionStore).

---

## Performance

- Use only per-account history; support efficient time-window queries; remove stale data to keep memory bounded. Tests enforce timing and scalability.
