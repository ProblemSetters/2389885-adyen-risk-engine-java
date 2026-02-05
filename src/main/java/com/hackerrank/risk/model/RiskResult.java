package com.hackerrank.risk.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiskResult {
    private final RiskStatus status;
    private final List<RiskReason> reasons;

    public RiskResult(RiskStatus status, List<RiskReason> reasons) {
        this.status = status;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
    }

    public RiskStatus getStatus() {
        return status;
    }

    public List<RiskReason> getReasons() {
        return reasons;
    }

    @Override
    public String toString() {
        if (status == RiskStatus.SAFE) {
            return "SAFE";
        } else {
            return "RISKY " + reasons.toString();
        }
    }
}
