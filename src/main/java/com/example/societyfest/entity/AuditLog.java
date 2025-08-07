package com.example.societyfest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;          // ADD_DONATION, EDIT_EXPENSE, etc.
    private String entityType;      // DONATION, EXPENSE, etc.
    private String entityId;        // Entity ID

    @Column(columnDefinition = "TEXT")
    private String beforeState;     // JSON snapshot

    @Column(columnDefinition = "TEXT")
    private String afterState;      // JSON snapshot

    private String ipAddress;
    private String userAgent;

    private LocalDateTime timestamp;
}
