package com.yowyob.crawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikimediaResponse {

    @JsonProperty("query")
    private Query query;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {

        @JsonProperty("pages")
        private Map<String, Page> pages;

        @JsonProperty("search")
        private List<SearchResult> search;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {

        @JsonProperty("title")
        private String title;

        @JsonProperty("imageinfo")
        private List<ImageInfo> imageinfo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageInfo {

        @JsonProperty("url")
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {

        @JsonProperty("title")
        private String title;
    }
}
