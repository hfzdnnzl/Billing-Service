package com.lakarra.billing.service;

import com.stripe.exception.StripeException;
import com.lakarra.billing.dto.PriceDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Tag("integration")
@DisplayName("Stripe Integration Tests (REAL API CALLS)")
class StripeIntegrationTest {

    @Autowired
    private StripeService stripeService;

    @Test
    @DisplayName("Should successfully connect to Stripe and fetch prices")
    void testRealStripeConnection() {
        try {
            // This test calls the REAL Stripe API using the key in src/test/resources/application.properties
            List<PriceDTO> prices = stripeService.getActivePrices();

            assertNotNull(prices, "Prices list should not be null");
            System.out.println("✅ CONNECTION SUCCESS: Successfully fetched " + prices.size() + " prices from Stripe.");
            
            for (PriceDTO price : prices) {
                System.out.println("\n - Product Name: " + price.getProductName());
                System.out.println(" - Price ID: " + price.getId() + ", Amount: " + price.getUnitAmount());
            }

        } catch (StripeException e) {
            fail("Failed to connect to real Stripe API. Check your internet connection and API Key. Error: " + e.getMessage());
        }
    }
}
