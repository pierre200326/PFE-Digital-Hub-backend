package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService,
                                AuditLogService auditLogService) {
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        if ("/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
            String clientIp = RequestUtils.getClientIp(request);

            if (loginAttemptService.isBlocked(clientIp)) {
                auditLogService.log(
                        "LOGIN_RATE_LIMIT_BLOCK",
                        "anonymous",
                        clientIp,
                        method,
                        request.getRequestURI(),
                        RequestUtils.getUserAgent(request),
                        "BLOCKED",
                        "Too many login attempts"
                );

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                    {"error":"Too many login attempts. Please try again later."}
                    """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}