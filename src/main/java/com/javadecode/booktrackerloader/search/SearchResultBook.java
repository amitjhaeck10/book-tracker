package com.javadecode.booktrackerloader.search;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResultBook {

    private String key;
    private String title;
    private int first_publish_year;
    private List<String> author_name;
    private String cover_i;
}
