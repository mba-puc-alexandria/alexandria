package com.pucsp.alexandria.adapter.in.rest.auth.dto;

public record RegisterRequest(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}
