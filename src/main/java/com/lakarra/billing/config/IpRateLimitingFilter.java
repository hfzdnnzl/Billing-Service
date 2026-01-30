package com.lakarra.billing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-IP rate limiting filter using an in-memory token bucket.<br/>
 * <br/>
 * - Limits to 100 requests per minute per IP (configurable via RATE and DURATION)<br/>
 * - Reads X-Forwarded-For header if present (common behind proxies/CDNs)<br/>
 * <br/>
 * Note: This in-memory implementation is suitable for single-instance or dev.
 * For production use behind multiple instances, use a distributed cache (Redis) or an API Gateway.
 */
@Component
public class IpRateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_TOKENS = 100;
    private static final Duration DURATION = Duration.ofMinutes(1);
    // tokens added per nanosecond
    private static final double TOKENS_PER_NANO = (double) MAX_TOKENS / (double) DURATION.toNanos();

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static class TokenBucket {
        private double tokens;
        private long lastRefillNano;

        TokenBucket() {
            this.tokens = MAX_TOKENS;
            this.lastRefillNano = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNano;
            if (elapsed <= 0) return;
            double toAdd = elapsed * TOKENS_PER_NANO;
            if (toAdd > 0) {
                tokens = Math.min(MAX_TOKENS, tokens + toAdd);
                lastRefillNano = now;
            }
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = clientIp(request);
        TokenBucket bucket = buckets.computeIfAbsent(ip, k -> new TokenBucket());

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"too_many_requests\",\"message\":\"Rate limit exceeded\"}");
        }
    }
}
