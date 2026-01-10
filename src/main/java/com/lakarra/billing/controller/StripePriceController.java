package com.lakarra.billing.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.lakarra.billing.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stripe/prices")
@RequiredArgsConstructor
@Slf4j
public class StripePriceController {

    private final StripeService stripeService;

    /**
     * Retrieve active prices from Stripe
     *
     * @return List of active prices
     */
    @GetMapping("/active")
    public ResponseEntity<List<Price>> getActivePrices() {
        try {
            log.info("Fetching active prices from Stripe");
            List<Price> prices = stripeService.getActivePrices();
            return ResponseEntity.ok(prices);
        } catch (StripeException e) {
            log.error("Error fetching active prices from Stripe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
