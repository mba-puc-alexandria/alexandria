package com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GutendexFormatsResponse(

    @JsonProperty("application/epub+zip")
    String applicationEpubZip,

    @JsonProperty("image/jpeg")
    String imageJpeg
) {

}
