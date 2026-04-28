package com.betrybe.alexandria.adapter.in.rest.dto;

public record CreateBookRequest(
    String title,
    String genre,
    Long publisherId
) {}

