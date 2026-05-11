package com.wenzhi.backend.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
  private final UserRepository userRepository;

  public MeController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public MeResponse me(Authentication authentication) {
    String username = authentication == null ? "" : String.valueOf(authentication.getPrincipal());
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return new MeResponse(username, null, null, null);
    return new MeResponse(user.getUsername(), user.getAvatarUrl(), user.getCoverUrl(), user.getBio());
  }

  @PutMapping
  public MeResponse updateMe(@RequestBody UpdateMeRequest req, Authentication authentication) {
    String username = authentication == null ? "" : String.valueOf(authentication.getPrincipal());
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return new MeResponse(username, null, null, null);
    String avatarUrl = req == null ? null : req.avatarUrl();
    String coverUrl = req == null ? null : req.coverUrl();
    String bio = req == null ? null : req.bio();
    user.setAvatarUrl(avatarUrl == null || avatarUrl.isBlank() ? null : avatarUrl.trim());
    user.setCoverUrl(coverUrl == null || coverUrl.isBlank() ? null : coverUrl.trim());
    user.setBio(bio == null || bio.isBlank() ? null : bio.trim());
    userRepository.save(user);
    return new MeResponse(user.getUsername(), user.getAvatarUrl(), user.getCoverUrl(), user.getBio());
  }

  public record MeResponse(String username, String avatarUrl, String coverUrl, String bio) {}

  public record UpdateMeRequest(String avatarUrl, String coverUrl, String bio) {}
}

