package com.example.backend.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String event,
        String actor,
        String ip,
        String method,
        String path,
        String userAgent,
        String status,
        String details,
        LocalDateTime timestamp
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getEvent(),
                log.getActor(),
                log.getIp(),
                log.getMethod(),
                log.getPath(),
                log.getUserAgent(),
                log.getStatus(),
                log.getDetails(),
                log.getTimestamp()
        );
    }
}