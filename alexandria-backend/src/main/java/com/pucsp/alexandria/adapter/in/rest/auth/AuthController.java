package com.pucsp.alexandria.adapter.in.rest.auth;

import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthResponse;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.GoogleAuthRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterResponse;
import com.pucsp.alexandria.application.auth.AuthenticateUserUseCase;
import com.pucsp.alexandria.application.auth.GoogleAuthUseCase;
import com.pucsp.alexandria.application.auth.RegisterUserUseCase;
import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final GoogleAuthUseCase googleAuthUseCase;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthController(RegisterUserUseCase registerUserUseCase,
                        AuthenticateUserUseCase authenticateUserUseCase,
                        GoogleAuthUseCase googleAuthUseCase,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
    this.registerUserUseCase = registerUserUseCase;
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.googleAuthUseCase = googleAuthUseCase;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
    var input = new RegisterInput(
        request.username(), request.firstName(), request.lastName(),
        request.email(), passwordEncoder.encode(request.password())
    );
    var output = registerUserUseCase.execute(input);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RegisterResponse.from(output));
  }

    @PostMapping("/google")
  public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
    try {
      var output = googleAuthUseCase.execute(request.credential());
      String token = jwtTokenProvider.generateToken(output.userId(), output.username(), output.role());
      return ResponseEntity.ok(AuthResponse.from(
          AuthOutput.of(token, output.userId(), output.username(), output.role())
      ));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    var input = new AuthInput(request.username(), request.password());
    var userOutput = authenticateUserUseCase.execute(input);

    String token = jwtTokenProvider.generateToken(userOutput.userId(), userOutput.username(), userOutput.role());

    return ResponseEntity.ok(AuthResponse.from(
        AuthOutput.of(token, userOutput.userId(), userOutput.username(), userOutput.role())
    ));
  }
}

