package com.wenzhi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.wenzhi.backend.security.JwtAuthFilter;
import org.springframework.http.MediaType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          res.getWriter().write("{\"message\":\"请先登录\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          res.getWriter().write("{\"message\":\"无权限\"}");
                        }))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // auth endpoints
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    // guest can read posts/comments
                    .requestMatchers(HttpMethod.GET, "/api/posts/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/posts/*/comments")
                    .permitAll()
                    // guest can view public user pages
                    .requestMatchers(HttpMethod.GET, "/api/users/**")
                    .permitAll()
                    // everything else under /api requires login
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll());
    return http.build();
  }
}

