package org.company.recaptchagate.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ip, String userAgent) {
        String key = ip + ":" + userAgent;

        return buckets.computeIfAbsent(key, k -> {
            Bandwidth perFiveMin = Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(5)));
            Bandwidth perDay = Bandwidth.classic(5, Refill.intervally(5, Duration.ofDays(1)));

            return Bucket.builder()
                    .addLimit(perFiveMin)
                    .addLimit(perDay)
                    .build();
        });
    }

    private String extractClientIp(HttpServletRequest request) {
        // Check for X-Forwarded-For header first (for production)
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            String ipWithPort = forwarded.split(",")[0].trim();
            return ipWithPort.split(":")[0].trim();
        }
        // Fallback to getRemoteAddr() for development or non-proxy environments
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = extractClientIp(request);  // Get the real client IP (either via X-Forwarded-For or getRemoteAddr)
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) userAgent = "unknown";

        Bucket bucket = resolveBucket(ip, userAgent);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            //CORS headers must be set manually here
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:63342");
            response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            String jsonResponse = """
        {
            "success": false,
            "pdfUrl": null,
            "reason": "Rate limit exceeded. Max 1 request per 5 minutes and 5 per day."
        }
        """;

            response.getWriter().write(jsonResponse);
            log.info("Rate limit exceeded for IP: {}, User-Agent: {}", ip, userAgent);
        }
    }
}
