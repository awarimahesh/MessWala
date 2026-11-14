package com.messwala.backend.controller;
import com.messwala.backend.dto.DuesSummaryResponse;
import com.messwala.backend.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final BillService billService;

    public OwnerController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/dues")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DuesSummaryResponse> getDues() {
        return ResponseEntity.ok(billService.getDues());
    }
}