package com.hackerrank.risk.engine;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.RiskStatus;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Collections;

public class RiskEngineImpl implements RiskEngine {
    private final TransactionStore store;

    public RiskEngineImpl() {
        this.store = new TransactionStore();
    }

    @Override
    public RiskResult evaluateTransaction(Transaction transaction) {
        // TODO: Replace with real rule evaluation (AmountAnomaly, store tx, HighFrequency, MerchantDiversity; then SAFE if no reasons else RISKY)
        store.storeTransaction(transaction);
        return new RiskResult(RiskStatus.RISKY, Collections.singletonList(RiskReason.MERCHANT_DIVERSITY));
    }

    public TransactionStore getStore() {
        return store;
    }
}
