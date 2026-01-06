package com.lakarra.billing.repository;

import com.lakarra.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {}
