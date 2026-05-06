package com.pucsp.alexandria.application.auth.dto;

public record RegisterInput(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}
