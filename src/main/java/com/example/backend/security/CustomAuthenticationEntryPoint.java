package com.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.example.backend.audit.AuditLogService;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuditLogService auditLogService;

    public CustomAuthenticationEntryPoint(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        auditLogService.log(
                "UNAUTHORIZED_ACCESS",
                "anonymous",
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "UNAUTHORIZED",
                "Authentication required or invalid token"
        );

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}