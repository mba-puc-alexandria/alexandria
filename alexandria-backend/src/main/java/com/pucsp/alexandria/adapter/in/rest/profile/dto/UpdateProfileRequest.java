package com.pucsp.alexandria.adapter.in.rest.profile.dto;

public record UpdateProfileRequest(
    String username,
    String firstName,
    String lastName
) {}
