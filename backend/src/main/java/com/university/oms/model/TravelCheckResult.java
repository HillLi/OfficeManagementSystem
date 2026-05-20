package com.university.oms.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TravelCheckResult {
    private BigDecimal standardAmount = BigDecimal.ZERO;
    private boolean overLimit;
    private List<String> messages = new ArrayList<String>();

    public BigDecimal getStandardAmount() {
        return standardAmount;
    }

    public void setStandardAmount(BigDecimal standardAmount) {
        this.standardAmount = standardAmount;
    }

    public boolean isOverLimit() {
        return overLimit;
    }

    public void setOverLimit(boolean overLimit) {
        this.overLimit = overLimit;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
