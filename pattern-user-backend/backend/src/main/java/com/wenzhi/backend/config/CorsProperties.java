package com.wenzhi.backend.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
  /**
   * Example:
   * app.cors.allowed-origins:
   *   - http://localhost:5173
   */
  private List<String> allowedOrigins = new ArrayList<>();

  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }
}

