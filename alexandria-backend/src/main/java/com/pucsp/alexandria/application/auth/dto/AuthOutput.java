package com.pucsp.alexandria.application.auth.dto;

public record AuthOutput(
    String token,
    String type,
    Long userId,
    String username
) {

  public static AuthOutput of(String token, Long userId, String username) {
    return new AuthOutput(token, "Bearer", userId, username);
  }
}
