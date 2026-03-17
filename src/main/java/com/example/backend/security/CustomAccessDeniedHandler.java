package com.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.example.backend.audit.AuditLogService;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditLogService auditLogService;

    public CustomAccessDeniedHandler(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : "unknown";

        auditLogService.log(
                "ACCESS_DENIED",
                actor,
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "FORBIDDEN",
                "Insufficient privileges"
        );

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
}