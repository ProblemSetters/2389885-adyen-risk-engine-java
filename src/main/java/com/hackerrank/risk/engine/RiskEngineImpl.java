package com.hackerrank.risk.engine;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.RiskStatus;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.rule.AmountAnomalyRule;
import com.hackerrank.risk.rule.HighFrequencyRule;
import com.hackerrank.risk.rule.MerchantDiversityRule;
import com.hackerrank.risk.rule.RiskRule;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RiskEngineImpl implements RiskEngine {
    private final TransactionStore store;

    public RiskEngineImpl() {
        this.store = new TransactionStore();
    }

    @Override
    public RiskResult evaluateTransaction(Transaction transaction) {
        List<RiskReason> triggeredReasons = new ArrayList<>();
        
        AmountAnomalyRule amountRule = new AmountAnomalyRule();
        Optional<RiskReason> amountReason = amountRule.evaluate(transaction, store);
        amountReason.ifPresent(triggeredReasons::add);
        
        store.storeTransaction(transaction);
        
        HighFrequencyRule frequencyRule = new HighFrequencyRule();
        Optional<RiskReason> frequencyReason = frequencyRule.evaluate(transaction, store);
        frequencyReason.ifPresent(triggeredReasons::add);
        
        MerchantDiversityRule diversityRule = new MerchantDiversityRule();
        Optional<RiskReason> diversityReason = diversityRule.evaluate(transaction, store);
        diversityReason.ifPresent(triggeredReasons::add);
        
        RiskStatus status = triggeredReasons.isEmpty() ? RiskStatus.SAFE : RiskStatus.RISKY;
        
        return new RiskResult(status, triggeredReasons);
    }

    public TransactionStore getStore() {
        return store;
    }
}
