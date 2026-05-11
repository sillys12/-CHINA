package com.wenzhi.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  /** HS256 secret. Use env var in production. */
  private String secret;

  /** Token TTL in seconds. Default 7 days. */
  private long ttlSeconds = 7 * 24 * 60 * 60;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getTtlSeconds() {
    return ttlSeconds;
  }

  public void setTtlSeconds(long ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
  }
}

