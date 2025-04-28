package com.rapidcrud.generator.controller;

import com.rapidcrud.generator.mongo.AuditLogDocument;
import com.rapidcrud.generator.mongo.AuditLogService;
import com.rapidcrud.generator.common.SortOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Log Management (MongoDB)", description = "APIs for listing and filtering audit logs via MongoDB")
@RequiredArgsConstructor
public class AuditLogMongoDBController {

    private final AuditLogService auditLogService;

    /**
     *  pagination query
     */
    @GetMapping("/page")
    @Operation(
            summary = "Paginated query audit logs",
            description = "Get paginated audit logs sorted by timestamp (default: DESC)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page query completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Page<AuditLogDocument> findAllByPage(
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return auditLogService.findAllByPage(page, size, sortOrder);
    }


    /**
     * condition query
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search audit logs by fields",
            description = "Search audit logs in MongoDB based on action, entity, and optional keyword matching."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<AuditLogDocument> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String keyword
    ) {
        return auditLogService.search(action, entity, keyword);
    }

    /**
     * batch saving to mongo db
     */
    @PostMapping("/batch")
    @Operation(
            summary = "Batch save audit logs",
            description = "Save multiple audit log documents to MongoDB at once."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch save successful"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<AuditLogDocument> saveAll(@RequestBody  @Parameter(description = "List of audit log documents to be saved") List<AuditLogDocument> documents) {
        return auditLogService.saveAll(documents);
    }
}

