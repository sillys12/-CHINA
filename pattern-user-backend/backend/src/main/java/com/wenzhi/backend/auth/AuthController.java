package com.wenzhi.backend.auth;

import com.wenzhi.backend.auth.dto.AuthOkResponse;
import com.wenzhi.backend.auth.dto.CheckUsernameResponse;
import com.wenzhi.backend.auth.dto.LoginRequest;
import com.wenzhi.backend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wenzhi.backend.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @GetMapping("/check-username")
  public CheckUsernameResponse checkUsername(@RequestParam("username") String username) {
    return new CheckUsernameResponse(authService.isUsernameAvailable(username));
  }

  @PostMapping("/register")
  public ResponseEntity<AuthOkResponse> register(@Valid @RequestBody RegisterRequest req) {
    try {
      authService.register(req);
      var username = req.getUsername() == null ? "" : req.getUsername().trim().toLowerCase(java.util.Locale.ROOT);
      var token = jwtService.issueToken(username);
      return ResponseEntity.ok(new AuthOkResponse(true, username, token));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthOkResponse(false, null, null));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthOkResponse> login(@Valid @RequestBody LoginRequest req) {
    var username = authService.loginAndGetUsername(req);
    if (username == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthOkResponse(false, null, null));
    }
    var token = jwtService.issueToken(username);
    return ResponseEntity.ok(new AuthOkResponse(true, username, token));
  }
}

