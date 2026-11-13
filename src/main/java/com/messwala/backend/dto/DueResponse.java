package com.messwala.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DueResponse {
    private Long billId;
    private String customerName;
    private String customerPhone;
    private String planName;
    private String month;
    private BigDecimal amount;
    private int skippedDays;
    private String paymentStatus;
    private LocalDate paidOn;

    public DueResponse(Long billId, String customerName, String customerPhone, String planName,
                       String month, BigDecimal amount, int skippedDays,
                       String paymentStatus, LocalDate paidOn) {
        this.billId = billId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.planName = planName;
        this.month = month;
        this.amount = amount;
        this.skippedDays = skippedDays;
        this.paymentStatus = paymentStatus;
        this.paidOn = paidOn;
    }

    public Long getBillId() { return billId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPlanName() { return planName; }
    public String getMonth() { return month; }
    public BigDecimal getAmount() { return amount; }
    public int getSkippedDays() { return skippedDays; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDate getPaidOn() { return paidOn; }
}