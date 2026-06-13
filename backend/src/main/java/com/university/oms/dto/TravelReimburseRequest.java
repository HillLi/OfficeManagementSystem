package com.university.oms.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 出差报销请求参数
 */
public class TravelReimburseRequest {
    /** 实际花费金额 */
    @NotNull(message = "实际费用不能为空")
    @DecimalMin(value = "0", message = "实际费用不能为负数")
    private BigDecimal actualExpense;
    /** 票据/凭证文件地址 */
    private String receiptUrl;
    /** 超预算原因说明 */
    private String overLimitReason;

    public BigDecimal getActualExpense() { return actualExpense; }
    public void setActualExpense(BigDecimal actualExpense) { this.actualExpense = actualExpense; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public String getOverLimitReason() { return overLimitReason; }
    public void setOverLimitReason(String overLimitReason) { this.overLimitReason = overLimitReason; }
}
