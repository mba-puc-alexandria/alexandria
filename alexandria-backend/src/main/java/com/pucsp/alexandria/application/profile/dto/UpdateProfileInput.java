package com.pucsp.alexandria.application.profile.dto;

public record UpdateProfileInput(
    String username,
    String firstName,
    String lastName
) {}
