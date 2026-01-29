package com.lakarra.billing.controller;

import com.stripe.exception.ApiException;
import com.lakarra.billing.dto.PriceDTO;
import com.lakarra.billing.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("StripePriceController Unit Tests")
class StripePriceControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private StripeService stripeService;

    private List<PriceDTO> mockPrices;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        PriceDTO mockPrice = PriceDTO.builder().id("price_123").unitAmount(35L).build();

        mockPrices = new ArrayList<>();
        mockPrices.add(mockPrice);
    }

    @Test
    @DisplayName("Should get active prices successfully")
    void testGetActivePrices_Success() throws Exception {
        // Arrange
        when(stripeService.getActivePrices()).thenReturn(mockPrices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/stripe/prices/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("price_123"))
                .andExpect(jsonPath("$[0].unitAmount").value(35L));
    }

    @Test
    @DisplayName("Should return 500 when StripeException occurs in getActivePrices")
    void testGetActivePrices_StripeException() throws Exception {
        // Arrange
        when(stripeService.getActivePrices()).thenThrow(new ApiException("API Error", "request_123", "code", 500, null));

        // Act & Assert
        mockMvc.perform(get("/api/v1/stripe/prices/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}
