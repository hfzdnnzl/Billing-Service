package com.lakarra.billing.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpRateLimitingFilterTest {

    private IpRateLimitingFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new IpRateLimitingFilter();
        chain = mock(FilterChain.class);
    }

    @Test
    void allowsPerIpLimitUntilBucketIsEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.1");

        for (int i = 0; i < 100; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, chain);
            assertNotEquals(429, response.getStatus(), "should not rate limit before bucket is empty");
        }

        verify(chain, times(100)).doFilter(any(), any());
        verifyNoMoreInteractions(chain);
    }

    @Test
    void respondsWith429AfterBucketIsExhausted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.5");

        for (int i = 0; i < 100; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, chain);

        assertEquals(429, rateLimitedResponse.getStatus());
        assertEquals("application/json", rateLimitedResponse.getContentType());
        assertTrue(rateLimitedResponse.getContentAsString().contains("too_many_requests"));
        verify(chain, times(100)).doFilter(any(), any());
        verifyNoMoreInteractions(chain);
    }
}

