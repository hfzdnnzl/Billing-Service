package com.lakarra.billing.controller;

import com.stripe.exception.StripeException;
import com.lakarra.billing.dto.PriceDTO;
import com.lakarra.billing.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripePriceController {

    private final StripeService stripeService;

    /**
     * Retrieve active prices from Stripe
     *
     * @return List of active prices with filtered fields
     */
    @GetMapping("/api/v1/stripe/prices/active")
    @PreAuthorize("hasAuthority('SCOPE_read:prices')")
    public ResponseEntity<List<PriceDTO>> getActivePrices() {
        try {
            log.info("Fetching active prices from Stripe");
            List<PriceDTO> prices = stripeService.getActivePrices();
            return ResponseEntity.ok(prices);
        } catch (StripeException e) {
            log.error("Error fetching active prices from Stripe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
