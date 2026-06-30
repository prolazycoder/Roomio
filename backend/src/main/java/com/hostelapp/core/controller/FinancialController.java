package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Invoice;
import com.hostelapp.core.entity.Payment;
import com.hostelapp.core.service.FinancialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/financial")
@RequiredArgsConstructor
@Tag(name = "Financial", description = "Invoice creation and payment recording")
@SecurityRequirement(name = "bearerAuth")
public class FinancialController {

    private final FinancialService financialService;

    @PostMapping("/invoices")
    @Operation(summary = "Create an invoice", description = "Generates a new invoice for a lease with optional line items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        return ResponseEntity.ok(financialService.createInvoice(invoice));
    }

    @PostMapping("/payments")
    @Operation(summary = "Record a payment", description = "Records an immutable payment ledger entry against an invoice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment recorded"),
            @ApiResponse(responseCode = "400", description = "Duplicate transaction reference or validation error")
    })
    public ResponseEntity<Payment> recordPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(financialService.recordPayment(payment));
    }
}

