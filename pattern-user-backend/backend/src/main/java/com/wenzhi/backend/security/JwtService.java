package com.wenzhi.backend.security;

import com.wenzhi.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtProperties props;
  private final SecretKey key;

  public JwtService(JwtProperties props) {
    this.props = props;
    var secret = props.getSecret() == null ? "" : props.getSecret().trim();
    if (secret.isBlank()) {
      // dev fallback; user should set app.jwt.secret via env var
      secret = "dev-only-secret-change-me-dev-only-secret-change-me";
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issueToken(String username) {
    var now = Instant.now();
    var exp = now.plusSeconds(Math.max(60, props.getTtlSeconds()));
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public String verifyAndGetSubject(String token) {
    Claims claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }
}

