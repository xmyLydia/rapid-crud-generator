package com.rapidcrud.generator.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "audit_dead_logs")
public class AuditDeadLetterDocument {

    @Id
    private String id;

    private String action;
    private String entity;
    private String payload;

    // original failed timestamp
    private LocalDateTime timestamp;

    private LocalDateTime deadLetteredAt;

    private String errorMessage;
}

