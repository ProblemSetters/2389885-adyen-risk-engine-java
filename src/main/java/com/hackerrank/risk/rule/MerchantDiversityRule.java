package com.hackerrank.risk.rule;

import com.hackerrank.risk.model.RiskReason;
import com.hackerrank.risk.model.Transaction;
import com.hackerrank.risk.storage.TransactionStore;

import java.util.Optional;

public class MerchantDiversityRule implements RiskRule {
    // TODO: Implement merchant diversity detection (e.g. > 3 unique merchants in 5-minute window)

    @Override
    public Optional<RiskReason> evaluate(Transaction transaction, TransactionStore store) {
        return Optional.empty();
    }
}
