package com.university.oms.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 差旅标准校验结果，用于出差申请时的费用标准检查
 */
public class TravelCheckResult {
    /** 按标准应报销金额 */
    private BigDecimal standardAmount = BigDecimal.ZERO;
    /** 是否超标 */
    private boolean overLimit;
    /** 校验提示信息列表 */
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
