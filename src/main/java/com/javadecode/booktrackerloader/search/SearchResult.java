package com.javadecode.booktrackerloader.search;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResult {

    private int numFound;
    private List<SearchResultBook> docs;

}
