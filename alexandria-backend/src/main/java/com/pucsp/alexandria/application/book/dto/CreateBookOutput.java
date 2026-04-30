package com.pucsp.alexandria.application.book.dto;

import java.util.List;

public record CreateBookOutput(
    List<Long> ids
) {}

