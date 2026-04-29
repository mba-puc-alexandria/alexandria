package com.pucsp.alexandria.application.book.dto;

public record UpdateBookInput(
    String title,
    String genre,
    Long publisherId
) {}

