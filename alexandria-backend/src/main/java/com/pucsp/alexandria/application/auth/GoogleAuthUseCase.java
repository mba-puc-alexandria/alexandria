package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.subscription.StartTrialUseCase;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

public class GoogleAuthUseCase {

  private static final String GOOGLE_TOKENINFO_URL =
      "https://oauth2.googleapis.com/tokeninfo?id_token=";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate;
  private final String googleClientId;
  private final StartTrialUseCase startTrialUseCase;

  public GoogleAuthUseCase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RestTemplate restTemplate,
      String googleClientId,
      StartTrialUseCase startTrialUseCase) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.restTemplate = restTemplate;
    this.googleClientId = googleClientId;
    this.startTrialUseCase = startTrialUseCase;
  }

    public record Output(Long userId, String username, String role) {}

  @SuppressWarnings("unchecked")
  public Output execute(String idToken) {
    Map<String, Object> payload;
    try {
      payload = restTemplate.getForObject(GOOGLE_TOKENINFO_URL + idToken, Map.class);
    } catch (Exception e) {
      throw new RuntimeException("Token Google inválido ou expirado");
    }

    if (payload == null) {
      throw new RuntimeException("Token Google inválido ou expirado");
    }

    String aud = (String) payload.get("aud");
    if (!googleClientId.equals(aud)) {
      throw new RuntimeException("Token Google não pertence a esta aplicação");
    }

    String email = (String) payload.get("email");
    String firstName = (String) payload.get("given_name");
    String lastName = (String) payload.get("family_name");

    return userRepository.findByEmail(email)
        .map(user -> new Output(user.getId().getValue(), user.getUsername(), user.getRole().name()))
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
          startTrialUseCase.execute(saved.getId().getValue());
          return new Output(saved.getId().getValue(), saved.getUsername(), saved.getRole().name());
        });
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
