package com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GutendexBookResponse(
    @JsonProperty("id")
    Long id,

    @JsonProperty("title")
    String title,

    @JsonProperty("authors")
    List<GutendexAuthorResponse> authors,

    @JsonProperty("formats")
    GutendexFormatsResponse formats,

    @JsonProperty("languages")
    List<String> languages,

    @JsonProperty("subjects")
    List<String> subjects,

    @JsonProperty("download_count")
    Integer downloadCount
) {

}

