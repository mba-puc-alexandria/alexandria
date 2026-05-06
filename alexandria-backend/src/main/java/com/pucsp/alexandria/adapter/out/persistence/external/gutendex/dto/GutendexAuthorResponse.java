package com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GutendexAuthorResponse(
    @JsonProperty("name")
    String name
) {

}
