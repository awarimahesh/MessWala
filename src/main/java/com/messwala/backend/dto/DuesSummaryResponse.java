package com.messwala.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DuesSummaryResponse {
    private int count;
    private BigDecimal totalPending;
    private List<DueResponse> dues;

    public DuesSummaryResponse(int count, BigDecimal totalPending, List<DueResponse> dues) {
        this.count = count;
        this.totalPending = totalPending;
        this.dues = dues;
    }

    public int getCount() { return count; }
    public BigDecimal getTotalPending() { return totalPending; }
    public List<DueResponse> getDues() { return dues; }
}