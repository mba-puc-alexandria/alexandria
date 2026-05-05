package com.pucsp.alexandria.adapter.in.rest.auth;

import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthResponse;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterResponse;
import com.pucsp.alexandria.application.auth.AuthenticateUserUseCase;
import com.pucsp.alexandria.application.auth.RegisterUserUseCase;
import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthController(RegisterUserUseCase registerUserUseCase,
                        AuthenticateUserUseCase authenticateUserUseCase,
                        AuthenticationManager authenticationManager,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
    this.registerUserUseCase = registerUserUseCase;
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.authenticationManager = authenticationManager;
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

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );

    var input = new AuthInput(request.username(), request.password());
    var userOutput = authenticateUserUseCase.execute(input);

    String token = jwtTokenProvider.generateToken(userOutput.userId(), userOutput.username());

    return ResponseEntity.ok(AuthResponse.from(
        AuthOutput.of(token, userOutput.userId(), userOutput.username())
    ));
  }
}
