package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credencial do Google (ID token) para login via OAuth.")
public record GoogleAuthRequest(
    @Schema(description = "ID token JWT emitido pelo Google OAuth.", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    String credential
) {}
