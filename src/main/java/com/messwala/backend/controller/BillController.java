package com.messwala.controller;

import com.messwala.dto.DueResponse;
import com.messwala.service.BillService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PutMapping("/{id}/paid")
    @PreAuthorize("hasRole('OWNER')")
    public DueResponse markPaid(@PathVariable Long id) {
        return billService.markPaid(id);
    }
}