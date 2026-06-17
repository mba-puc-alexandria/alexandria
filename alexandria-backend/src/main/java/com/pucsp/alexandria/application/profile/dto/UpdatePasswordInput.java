package com.pucsp.alexandria.application.profile.dto;

public record UpdatePasswordInput(
    String currentPassword,
    String newPassword
) {}
