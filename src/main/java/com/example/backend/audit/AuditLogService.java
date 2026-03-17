package com.example.backend.audit;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String event,
                    String actor,
                    String ip,
                    String method,
                    String path,
                    String userAgent,
                    String status,
                    String details) {

        AuditLog log = new AuditLog();
        log.setEvent(safe(event));
        log.setActor(safe(actor));
        log.setIp(safe(ip));
        log.setMethod(safe(method));
        log.setPath(safe(path));
        log.setUserAgent(safe(userAgent));
        log.setStatus(safe(status));
        log.setDetails(safe(details));
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[\\r\\n]", "_");
    }
}