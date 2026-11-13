package com.messwala.controller;

import com.messwala.dto.DuesSummaryResponse;
import com.messwala.service.BillService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final BillService billService;

    public OwnerController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/dues")
    @PreAuthorize("hasRole('OWNER')")
    public DuesSummaryResponse getDues() {
        return billService.getDues();
    }
}