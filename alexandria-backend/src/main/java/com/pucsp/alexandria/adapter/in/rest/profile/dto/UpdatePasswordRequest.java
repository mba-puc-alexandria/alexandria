package com.pucsp.alexandria.adapter.in.rest.profile.dto;

public record UpdatePasswordRequest(
    String currentPassword,
    String newPassword
) {}
