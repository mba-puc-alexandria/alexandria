package com.pucsp.alexandria.application.book.dto;

public record CreateBookInput(
    String title,
    String genre,
    Long publisherId
) {}

