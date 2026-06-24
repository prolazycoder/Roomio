package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Invoice;
import com.hostelapp.core.entity.Payment;
import com.hostelapp.core.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        return ResponseEntity.ok(financialService.createInvoice(invoice));
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(financialService.recordPayment(payment));
    }
}
