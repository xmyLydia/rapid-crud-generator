package com.rapidcrud.generator.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> data;
}

