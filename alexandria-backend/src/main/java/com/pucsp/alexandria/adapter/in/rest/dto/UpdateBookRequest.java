package com.pucsp.alexandria.adapter.in.rest.dto;

public record UpdateBookRequest(
    String title,
    String genre,
    Long publisherId
) {}

