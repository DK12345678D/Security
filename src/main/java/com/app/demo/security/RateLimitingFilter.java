package com.app.demo.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.demo.service.AuditLogService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    private final ConcurrentHashMap<String, RequestCounter> limitMap = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 3;
    private static final long WINDOW_MS = 60000;

    private static class RequestCounter {
        final long resetTime;
        final AtomicInteger count = new AtomicInteger(0);

        RequestCounter(long resetTime) {
            this.resetTime = resetTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > resetTime;
        }
    }

    public void clearRateLimits() {
        limitMap.clear();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth/")) {
            String ip = request.getRemoteAddr();
            long now = System.currentTimeMillis();

            RequestCounter counter = limitMap.compute(ip, (key, value) -> {
                if (value == null || value.isExpired()) {
                    RequestCounter newCounter = new RequestCounter(now + WINDOW_MS);
                    newCounter.count.incrementAndGet();
                    return newCounter;
                }
                value.count.incrementAndGet();
                return value;
            });

            if (counter.count.get() > MAX_REQUESTS) {

                auditLogService.log(
                        "SYSTEM",
                        "SYSTEM",
                        ip,
                        "Authentication",
                        "Rate limit exceeded: IP " + ip + " blocked on " + path,
                        "WEB",
                        "Rate Limiting");

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
