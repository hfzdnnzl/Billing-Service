package com.lakarra.billing.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceListParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * Retrieve active prices from Stripe
     *
     * @return List of active Price objects
     * @throws StripeException if Stripe API call fails
     */
    public List<Price> getActivePrices() throws StripeException {
        Stripe.apiKey = stripeApiKey;
        PriceListParams params = PriceListParams.builder()
                .setActive(true)
                .build();
        PriceCollection prices = Price.list(params);
        return prices.getData();
    }

}
