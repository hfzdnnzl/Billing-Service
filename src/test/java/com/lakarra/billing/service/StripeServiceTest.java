package com.lakarra.billing.service;

import com.stripe.exception.StripeException;
import com.stripe.exception.ApiException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceListParams;
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

    private List<Price> mockPrices;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeService, "stripeApiKey", "sk_test_123456");

        Price mockPrice = new Price();
        mockPrice.setId("price_123");
        
        mockPrices = new ArrayList<>();
        mockPrices.add(mockPrice);
    }

    @Test
    @DisplayName("Should retrieve active prices successfully")
    void testGetActivePrices_Success() throws StripeException {
        // Arrange
        when(mockPriceCollection.getData()).thenReturn(mockPrices);
        
        try (MockedStatic<com.stripe.model.Price> mockedPrice = mockStatic(com.stripe.model.Price.class)) {
            mockedPrice.when(() -> com.stripe.model.Price.list(any(PriceListParams.class))).thenReturn(mockPriceCollection);

            // Act
            List<Price> result = stripeService.getActivePrices();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    @DisplayName("Should handle StripeException when retrieving active prices")
    void testGetActivePrices_StripeException() throws StripeException {
        // Arrange
        try (MockedStatic<com.stripe.model.Price> mockedPrice = mockStatic(com.stripe.model.Price.class)) {
            mockedPrice.when(() -> com.stripe.model.Price.list(any(PriceListParams.class)))
                    .thenThrow(new ApiException("API Error", "request_123", "code", 500, null));

            // Act & Assert
            assertThrows(StripeException.class, () -> stripeService.getActivePrices());
        }
    }

}
