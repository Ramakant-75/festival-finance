package com.example.societyfest.service;

import com.example.societyfest.entity.AuditLog;
import com.example.societyfest.repository.AuditLogRepository;
import com.example.societyfest.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
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

        AuditLog auditlog = AuditLog.builder()
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
        try {
            auditLogRepository.save(auditlog);
        }catch (Exception e){
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "UNKNOWN";
    }
}
