package com.hostelapp.core.service.impl;

import com.hostelapp.core.entity.Invoice;
import com.hostelapp.core.entity.Payment;
import com.hostelapp.core.repository.InvoiceRepository;
import com.hostelapp.core.repository.PaymentRepository;
import com.hostelapp.core.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialServiceImpl implements FinancialService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getItems() != null) {
            invoice.getItems().forEach(item -> item.setInvoice(invoice));
        }
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Payment recordPayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        if (payment.getInvoice() != null) {
            Invoice invoice = invoiceRepository.findById(payment.getInvoice().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + payment.getInvoice().getId()));
            
            invoice.setStatus("PAID");
            invoiceRepository.save(invoice);
            savedPayment.setInvoice(invoice);
        }

        return savedPayment;
    }
}
