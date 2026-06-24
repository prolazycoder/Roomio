package com.hostelapp.core.service;

import com.hostelapp.core.entity.Invoice;
import com.hostelapp.core.entity.Payment;

public interface FinancialService {
    Invoice createInvoice(Invoice invoice);
    Payment recordPayment(Payment payment);
}
