package com.pucsp.alexandria.adapter.in.rest.auth.dto;

public record AuthRequest(
    String username,
    String password
) {}
