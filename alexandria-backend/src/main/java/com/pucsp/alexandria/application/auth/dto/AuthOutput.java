package com.pucsp.alexandria.application.auth.dto;

public record AuthOutput(
    String token,
    String type,
    Long userId,
    String username,
    String role
) {

  public static AuthOutput of(String token, Long userId, String username, String role) {
    return new AuthOutput(token, "Bearer", userId, username, role);
  }
}
