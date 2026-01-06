package com.lakarra.billing.controller;

import com.lakarra.billing.entity.Invoice;
import com.lakarra.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing invoices.
 * <p>
 * Provides endpoints to retrieve all invoices and create a new invoice.
 * </p>
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Retrieves all invoices from the database.
     * @return a list of all invoices
     */
    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    /**
     * Creates a new invoice and saves it to the database.
     * @param invoice the invoice to create
     * @return the saved invoice
     */
    @PostMapping
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return invoiceService.createInvoice(invoice);
    }
}


