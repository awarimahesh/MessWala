package com.messwala.backend.controller;

import com.messwala.backend.dto.BillResponse;
import com.messwala.backend.service.BillService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/owner/bills")
public class OwnerBillController {

    private final BillService billService;

    public OwnerBillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<BillResponse>> generate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "true") boolean giveCredit) {
        return ResponseEntity.ok(billService.generate(month, giveCredit));
    }
}