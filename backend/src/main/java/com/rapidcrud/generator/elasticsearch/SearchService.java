package com.rapidcrud.generator.elasticsearch;

import java.io.IOException;

public interface SearchService<T> {

    SearchResult<T> search(AuditLogSearchRequest request) throws IOException;
}

