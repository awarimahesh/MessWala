package com.messwala.backend.controller;
import com.messwala.backend.dto.BillResponse;
import com.messwala.backend.dto.DueResponse;
import com.messwala.backend.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // Day 14: customer apne bills dekhe
    @GetMapping("/my")
    public ResponseEntity<List<BillResponse>> myBills(Authentication authentication) {
        return ResponseEntity.ok(billService.myBills(authentication.getName()));
    }

    // Day 15: owner bill PAID mark kare
    @PutMapping("/{id}/paid")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DueResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billService.markPaid(id));
    }
}