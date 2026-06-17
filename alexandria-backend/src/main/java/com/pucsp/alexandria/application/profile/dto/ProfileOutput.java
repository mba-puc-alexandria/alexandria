package com.pucsp.alexandria.application.profile.dto;

import java.time.LocalDateTime;

public record ProfileOutput(
    Long userId,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDateTime createdAt
) {}
