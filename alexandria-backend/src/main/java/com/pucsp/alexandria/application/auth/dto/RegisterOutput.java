package com.pucsp.alexandria.application.auth.dto;

public record RegisterOutput(
    Long id,
    String username,
    String email
) {}
