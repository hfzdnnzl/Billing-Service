package com.lakarra.billing.service;

import com.stripe.exception.StripeException;
import com.stripe.exception.ApiException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceListParams;
import com.lakarra.billing.dto.PriceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeService Unit Tests")
class StripeServiceTest {

    @InjectMocks
    private StripeService stripeService;

    @Mock
    private PriceCollection mockPriceCollection;

    @Mock
    private Price mockPrice;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeService, "stripeApiKey", "sk_test_123456");
    }

    @Test
    @DisplayName("Should retrieve active prices successfully")
    void testGetActivePrices_Success() throws StripeException {
        // Arrange
        when(mockPrice.getId()).thenReturn("price_123");
        when(mockPrice.getUnitAmount()).thenReturn(3500L); // In cents
        when(mockPrice.getCurrency()).thenReturn("myr");
        when(mockPrice.getProduct()).thenReturn("prod_123");
        when(mockPrice.getProductObject()).thenReturn(null);

        List<Price> mockPrices = new ArrayList<>();
        mockPrices.add(mockPrice);

        when(mockPriceCollection.getData()).thenReturn(mockPrices);
        
        try (MockedStatic<Price> mockedPrice = mockStatic(Price.class)) {
            mockedPrice.when(() -> Price.list(any(PriceListParams.class))).thenReturn(mockPriceCollection);

            // Act
            List<PriceDTO> result = stripeService.getActivePrices();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("price_123", result.getFirst().getId());
            assertEquals(35L, result.getFirst().getUnitAmount()); // 3500 cents / 100 = 35
        }
    }

    @Test
    @DisplayName("Should handle StripeException when retrieving active prices")
    void testGetActivePrices_StripeException() {
        // Arrange
        try (MockedStatic<Price> mockedPrice = mockStatic(Price.class)) {
            mockedPrice.when(() -> Price.list(any(PriceListParams.class)))
                    .thenThrow(new ApiException("API Error", "request_123", "code", 500, null));

            // Act & Assert
            assertThrows(StripeException.class, () -> stripeService.getActivePrices());
        }
    }

}
