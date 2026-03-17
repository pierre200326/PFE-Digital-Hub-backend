package com.example.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void log(String event,
                    String actor,
                    String ip,
                    String method,
                    String path,
                    String userAgent,
                    String status,
                    String details) {

        auditLogger.info(
                "event={} actor={} ip={} method={} path={} userAgent={} status={} details={}",
                safe(event),
                safe(actor),
                safe(ip),
                safe(method),
                safe(path),
                safe(userAgent),
                safe(status),
                safe(details)
        );
    }

    public void logSimple(String event,
                          String actor,
                          String ip,
                          String status,
                          String details) {
        log(event, actor, ip, "unknown", "unknown", "unknown", status, details);
    }

    private String safe(String value) {
        return value == null ? "unknown" : value.replaceAll("[\\r\\n]", "_");
    }
}