package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final GlobalRateLimitService globalRateLimitService;

    public GlobalRateLimitFilter(GlobalRateLimitService globalRateLimitService) {
        this.globalRateLimitService = globalRateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // On laisse /auth/login à son filtre spécifique anti-brute-force
        if ("/auth/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        if (globalRateLimitService.isBlocked(clientIp)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                {"error":"Too many requests. Please try again later."}
                """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}