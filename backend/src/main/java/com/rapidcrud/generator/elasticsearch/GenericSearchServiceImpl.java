package com.rapidcrud.generator.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.rapidcrud.generator.utils.ElasticsearchQueryUtils.buildHighlight;
import static com.rapidcrud.generator.utils.ElasticsearchQueryUtils.buildSortOptions;

@Service
@RequiredArgsConstructor
public class GenericSearchServiceImpl implements SearchService<Map<String, Object>> {
    private final ElasticsearchClient elasticsearchClient;

    public static final List<String> ALLOWED_FIELDS = List.of("timestamp", "action", "entity");

    @Override
    public SearchResult<Map<String, Object>> search(AuditLogSearchRequest req) throws IOException {
        final BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (StringUtils.hasText(req.getAction())) {
            boolBuilder.must(mq -> mq.match(m -> m.field("action").query(req.getAction())));
        }

        if (StringUtils.hasText(req.getEntity())) {
            boolBuilder.must(mq -> mq.match(m -> m.field("entity").query(req.getEntity())));
        }

        if (StringUtils.hasText(req.getKeyword())) {
            boolBuilder.should(mq -> mq.match(m -> m.field("payload").query(req.getKeyword())));
        }

        final Query query = Query.of(q -> q.bool(boolBuilder.build()));

        // search request
        final SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("audit-logs")
                .from(req.getPage() * req.getSize())
                .size(req.getSize())
                .query(query)
                .sort(buildSortOptions(req.getSortBy(), String.valueOf(req.getSortOrder()), ALLOWED_FIELDS))
                .highlight(buildHighlight(req.getHighlightFields()))
        );

        @SuppressWarnings({"unchecked"})
        final SearchResponse<Map<String, Object>> response =
                (SearchResponse<Map<String, Object>>) (SearchResponse<?>) elasticsearchClient.search(searchRequest, Map.class);

        final List<Map<String, Object>> result =  response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> source = hit.source();
                    if (source == null) return null;

                    Map<String, List<String>> highlight = hit.highlight();
                    if (highlight != null) {
                        highlight.forEach((field, fragments) -> {
                            if (!fragments.isEmpty()) {
                                source.put("highlighted_" + field, fragments.get(0));
                            }
                        });
                    }
                    return source;
                })
                .filter(Objects::nonNull)
                .toList();
        final long total = Optional.ofNullable(response.hits().total())
                .map(TotalHits::value)
                .orElse((long) result.size());

        return new SearchResult<>(
                total,
                req.getPage(),
                req.getSize(),
                result
        );
    }
}
