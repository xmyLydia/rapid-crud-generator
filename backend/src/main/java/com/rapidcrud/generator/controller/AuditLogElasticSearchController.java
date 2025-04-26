package com.rapidcrud.generator.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.rapidcrud.generator.elasticsearch.AuditLogSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Audit Log Search (Elasticsearch)", description = "APIs for searching audit logs via Elasticsearch based on keywords")
@RequiredArgsConstructor
public class AuditLogElasticSearchController {

    private final ElasticsearchClient elasticsearchClient;

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
    public List<Map<String, Object>> search(@RequestBody AuditLogSearchRequest req) throws IOException {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (StringUtils.hasText(req.getAction())) {
            boolBuilder.must(mq -> mq.match(m -> m.field("action").query(req.getAction())));
        }

        if (StringUtils.hasText(req.getEntity())) {
            boolBuilder.must(mq -> mq.match(m -> m.field("entity").query(req.getEntity())));
        }

        if (StringUtils.hasText(req.getKeyword())) {
            boolBuilder.should(mq -> mq.match(m -> m.field("payload").query(req.getKeyword())));
        }

        Query query = Query.of(q -> q.bool(boolBuilder.build()));

        // search request
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("audit-logs")
                .from(req.getPage() * req.getSize())
                .size(req.getSize())
                .query(query)
        );

        @SuppressWarnings({"unchecked"})
        SearchResponse<Map<String, Object>> response =
                (SearchResponse<Map<String, Object>>) (SearchResponse<?>) elasticsearchClient.search(searchRequest, Map.class);

        return response.hits().hits().stream()
                .map(hit -> (Map<String, Object>) hit.source())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
