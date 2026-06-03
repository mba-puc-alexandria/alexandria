package com.pucsp.alexandria.adapter.in.rest.dto;

public record AddUserBooksRequest(
    Long bookId,
    String status
) {}
