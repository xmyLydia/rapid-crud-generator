package com.rapidcrud.generator.controller;

import com.rapidcrud.generator.elasticsearch.AuditLogSearchRequest;
import com.rapidcrud.generator.elasticsearch.GenericSearchServiceImpl;
import com.rapidcrud.generator.elasticsearch.SearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/logs/elasticsearch")
@Tag(name = "Audit Log Search (Elasticsearch)", description = "APIs for searching audit logs via Elasticsearch based on keywords")
@RequiredArgsConstructor
public class AuditLogElasticSearchController {

    private final GenericSearchServiceImpl genericSearchService;

    @PostMapping("/search")
    @Operation(
            summary = "Search audit logs by keyword",
            description = "Perform full-text search on audit logs via Elasticsearch. Suitable for fuzzy search scenarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public SearchResult<Map<String, Object>> search(@RequestBody AuditLogSearchRequest req) throws IOException {
        return genericSearchService.search(req);
    }

}
