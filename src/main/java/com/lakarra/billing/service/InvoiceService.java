package com.lakarra.billing.service;

import com.lakarra.billing.entity.Invoice;
import com.lakarra.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing invoices and their business lifecycle.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    /**
     * Retrieves all registered invoices.
     * @return a list of all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Processes and saves a new invoice.
     * Metadata like createdAt and invoiceNumber are handled automatically via JPA Auditing and Lifecycle hooks.
     * @param invoice the invoice data to be processed
     * @return the saved invoice with generated information
     */
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }
}

