package com.lakarra.billing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration tests for {@link InvoiceController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/invoices";

    /**
     * Tests the retrieval of all invoices.
     * Expects a 200 OK status.
     */
    @Test
    @WithMockUser
    void testGetInvoices() throws Exception {
        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());
    }

    /**
     * Tests the creation of a new invoice via POST.
     * Verifies that the created invoice contains the correct data.
     */
    @Test
    @WithMockUser
    void testCreateInvoice() throws Exception {
        String json = """
            {
                "userId": "user123",
                "cardId": "card456",
                "packageType": "BASIC",
                "addonType": "DEFAULT",
                "amount": 25.00,
                "currency": "MYR"
            }
            """;

        mockMvc.perform(post(BASE_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.cardId").value("card456"))
                .andExpect(jsonPath("$.packageType").value("BASIC"))
                .andExpect(jsonPath("$.addonType").value("DEFAULT"))
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.currency").value("MYR"))
                .andExpect(jsonPath("$.invoiceNumber").exists())
                .andExpect(jsonPath("$.paid").value(false));
    }
}
