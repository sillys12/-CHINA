package com.wenzhi.backend.auth;

import com.wenzhi.backend.auth.dto.LoginRequest;
import com.wenzhi.backend.auth.dto.RegisterRequest;
import com.wenzhi.backend.user.UserEntity;
import com.wenzhi.backend.user.UserRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public boolean isUsernameAvailable(String username) {
    var u = normalizeUsername(username);
    if (u.isBlank()) return false;
    return !userRepository.existsByUsername(u);
  }

  public void register(RegisterRequest req) {
    var username = normalizeUsername(req.getUsername());
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("用户名已存在");
    }

    var entity = new UserEntity();
    entity.setUsername(username);
    entity.setPassword(passwordEncoder.encode(req.getPassword()));
    try {
      userRepository.save(entity);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("用户名已存在");
    }
  }

  public String loginAndGetUsername(LoginRequest req) {
    var username = normalizeUsername(req.getUsername());
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return null;
    return passwordEncoder.matches(req.getPassword(), user.getPassword()) ? username : null;
  }

  private String normalizeUsername(String username) {
    return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
  }
}

