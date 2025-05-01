package com.rapidcrud.generator.utils;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.search.Highlight;

import java.util.List;

public class ElasticsearchQueryUtils {

    public static Highlight buildHighlight(List<String> fields) {
        if (fields == null || fields.isEmpty()) return null;
        return Highlight.of(h -> {
            for (String field : fields) {
                h.fields(field, hf -> hf.preTags("<em>").postTags("</em>"));
            }
            return h;
        });
    }

    public static SortOptions buildSortOptions(String field, String order, List<String> allowedFields) {
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + field);
        }
        return SortOptions.of(so -> so.field(f -> f
                .field(field)
                .order("asc".equalsIgnoreCase(order) ? SortOrder.Asc : SortOrder.Desc)
        ));
    }
}
