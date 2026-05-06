package com.pucsp.alexandria.adapter.in.rest.dto;

public record UpdateUserBooksRequest(
    String status,
    Integer progress,
    Integer rating
) {}
