package com.lakarra.billing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakarra.billing.entity.Invoice;
import com.lakarra.billing.entity.PackageType;
import com.lakarra.billing.entity.AddonType;
import java.math.BigDecimal;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/invoices";

    @Test
    @WithMockUser
    void testGetInvoices() throws Exception {
        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateInvoice() throws Exception {
        Invoice invoice = new Invoice(
            "user123", "card456", PackageType.BASIC, 
            AddonType.DEFAULT, new BigDecimal("25.00"), "MYR");
        String json = objectMapper.writeValueAsString(invoice);

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

    @TestConfiguration
    static class InvoiceControllerTestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }
    }
}
