package com.lakarra.billing.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceListParams;
import com.lakarra.billing.dto.PriceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * Retrieve active prices from Stripe and return filtered data
     *
     * @return List of filtered PriceDTO objects containing only essential fields
     * @throws StripeException if Stripe API call fails
     */
    public List<PriceDTO> getActivePrices() throws StripeException {
        Stripe.apiKey = stripeApiKey;
        PriceListParams params = PriceListParams.builder().setActive(true).addExpand("data.product").build();
        PriceCollection prices = Price.list(params);
        return prices.getData().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Convert Stripe Price object to PriceDTO
     *
     * @param price Stripe Price object
     * @return PriceDTO with selected fields
     */
    private PriceDTO convertToDTO(Price price) {
        return PriceDTO.builder()
                .id(price.getId())
                .productId(price.getProduct())
                .productName(price.getProductObject() != null ? price.getProductObject().getName() : null)
                .unitAmount(price.getUnitAmount()/100) // Convert cents to dollars
                .currency(price.getCurrency())
                .build();
    }

}
