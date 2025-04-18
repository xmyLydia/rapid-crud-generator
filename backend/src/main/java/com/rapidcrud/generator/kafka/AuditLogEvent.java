package com.rapidcrud.generator.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {
    private String action;
    private String entity;
    private String payload;
    private LocalDateTime timestamp;
}

