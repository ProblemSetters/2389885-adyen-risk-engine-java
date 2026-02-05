package com.hackerrank.risk.engine;

import com.hackerrank.risk.model.RiskResult;
import com.hackerrank.risk.model.Transaction;

public interface RiskEngine {
    RiskResult evaluateTransaction(Transaction transaction);
}
