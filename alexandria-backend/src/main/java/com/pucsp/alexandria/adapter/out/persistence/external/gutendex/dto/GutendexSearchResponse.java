package com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GutendexSearchResponse(
    @JsonProperty("count")
    Integer count,

    @JsonProperty("results")
    List<GutendexBookResponse> results
) {

}
