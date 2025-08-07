package com.example.societyfest.service;

import com.example.societyfest.entity.AuditLog;
import com.example.societyfest.repository.AuditLogRepository;
import com.example.societyfest.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logChange(String action,
                          String entityType,
                          String entityId,
                          Object beforeState,
                          Object afterState,
                          HttpServletRequest request) {
        String username = getCurrentUsername();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .beforeState(JsonUtil.toJson(beforeState))
                .afterState(JsonUtil.toJson(afterState))
                .ipAddress(ip)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "UNKNOWN";
    }
}
