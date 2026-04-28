package com.betrybe.alexandria.application.book.dto;

public record UpdateBookInput(
    String title,
    String genre,
    Long publisherId
) {}

