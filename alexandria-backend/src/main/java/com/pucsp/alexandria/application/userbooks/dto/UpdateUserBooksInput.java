package com.pucsp.alexandria.application.userbooks.dto;

public record UpdateUserBooksInput(
    String status,
    Integer progress,
    Integer rating
) {}
