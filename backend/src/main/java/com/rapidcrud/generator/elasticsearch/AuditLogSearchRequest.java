package com.rapidcrud.generator.elasticsearch;

import com.rapidcrud.generator.common.SortOrder;
import lombok.Data;

import java.util.List;

@Data
public class AuditLogSearchRequest {
    private String action;     // Optional, supports fuzzy search
    private String entity;     // Optional, exact match or fuzzy
    private String keyword;    // Keyword to search within the payload
    private int page = 0;      // Page number (zero-based)
    private int size = 10;     // Page size (number of results per page)
    private String sortBy = "timestamp";
    private SortOrder sortOrder = SortOrder.DESC;
    private List<String> highlightFields = List.of("payload");
}
