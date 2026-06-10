package com.pucsp.alexandria.application.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GoogleAuthUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final String googleClientId;

  public GoogleAuthUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, String googleClientId) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.googleClientId = googleClientId;
  }

  public record Output(Long userId, String username) {}

  public Output execute(String idTokenString) {
    GoogleIdToken.Payload payload = verifyToken(idTokenString);

    String email = payload.getEmail();
    String firstName = (String) payload.get("given_name");
    String lastName = (String) payload.get("family_name");

    return userRepository.findByEmail(email)
        .map(user -> new Output(user.getId().getValue(), user.getUsername()))
        .orElseGet(() -> {
          String username = generateUsername(email);
          String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
          User newUser = User.create(
              username,
              firstName != null && !firstName.isBlank() ? firstName : "User",
              lastName != null && !lastName.isBlank() ? lastName : "Google",
              email,
              randomPassword
          );
          User saved = userRepository.save(newUser);
          return new Output(saved.getId().getValue(), saved.getUsername());
        });
  }

  private GoogleIdToken.Payload verifyToken(String idTokenString) {
    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
          new NetHttpTransport(), new GsonFactory())
          .setAudience(Collections.singletonList(googleClientId))
          .build();
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        throw new RuntimeException("Token Google inválido ou expirado");
      }
      return idToken.getPayload();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao verificar token Google: " + e.getMessage(), e);
    }
  }

  private String generateUsername(String email) {
    String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
    if (base.length() < 3) base = base + "_user";
    String username = base;
    int attempt = 1;
    while (userRepository.existsByUsername(username)) {
      username = base + attempt++;
    }
    return username;
  }
}
