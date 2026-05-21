package com.university.oms.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class TravelReimburseRequest {
    @NotNull(message = "实际费用不能为空")
    private BigDecimal actualExpense;
    private String receiptUrl;

    public BigDecimal getActualExpense() { return actualExpense; }
    public void setActualExpense(BigDecimal actualExpense) { this.actualExpense = actualExpense; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
}
