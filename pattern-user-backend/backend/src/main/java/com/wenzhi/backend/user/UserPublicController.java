package com.wenzhi.backend.user;

import com.wenzhi.backend.square.PostController;
import com.wenzhi.backend.square.PostFavoriteRepository;
import com.wenzhi.backend.square.PostImageRepository;
import com.wenzhi.backend.square.PostLikeRepository;
import com.wenzhi.backend.square.PostRepository;
import com.wenzhi.backend.square.UserPostKey;
import com.wenzhi.backend.square.CommentRepository;
import com.wenzhi.backend.upload.QiniuUrlService;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserPublicController {
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final PostLikeRepository likeRepository;
  private final PostFavoriteRepository favoriteRepository;
  private final CommentRepository commentRepository;
  private final QiniuUrlService urlService;

  public UserPublicController(
      UserRepository userRepository,
      PostRepository postRepository,
      PostImageRepository postImageRepository,
      PostLikeRepository likeRepository,
      PostFavoriteRepository favoriteRepository,
      CommentRepository commentRepository,
      QiniuUrlService urlService) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.postImageRepository = postImageRepository;
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.commentRepository = commentRepository;
    this.urlService = urlService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> user(@PathVariable("id") Long id) {
    var u = userRepository.findById(id).orElse(null);
    if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("用户不存在"));
    return ResponseEntity.ok(new UserResponse(u.getId(), u.getUsername(), u.getAvatarUrl(), u.getCoverUrl(), u.getBio()));
  }

  @GetMapping("/{id}/posts")
  public ResponseEntity<?> posts(
      @PathVariable("id") Long id,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      Authentication authentication) {
    var u = userRepository.findById(id).orElse(null);
    if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("用户不存在"));

    int safeSize = Math.max(1, Math.min(50, size));
    int safePage = Math.max(0, page);
    var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    String username = principal(authentication);
    Long viewerUserId =
        username == null ? null : userRepository.findByUsername(username).map(UserEntity::getId).orElse(null);

    var p = postRepository.findByAuthorId(id, pageable);
    var items = p.getContent().stream().map(pe -> toPostResponse(pe, viewerUserId)).toList();
    return ResponseEntity.ok(new PostController.PostPageResponse(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }

  private PostController.PostResponse toPostResponse(com.wenzhi.backend.square.PostEntity p, Long viewerUserId) {
    Long postId = p.getId();
    var imgs =
        postImageRepository.findByPostIdOrderBySortOrderAscIdAsc(postId).stream()
            .map(com.wenzhi.backend.square.PostImageEntity::getUrl)
            .map(urlService::signIfPossible)
            .toList();
    long likes = likeRepository.countByIdPostId(postId);
    long favorites = favoriteRepository.countByIdPostId(postId);
    long comments = commentRepository.countByPostId(postId);

    boolean liked = false;
    boolean faved = false;
    if (viewerUserId != null) {
      liked = likeRepository.existsById(new UserPostKey(viewerUserId, postId));
      faved = favoriteRepository.existsById(new UserPostKey(viewerUserId, postId));
    }

    var author = p.getAuthor();
    return new PostController.PostResponse(
        postId,
        author.getId(),
        author.getUsername(),
        author.getAvatarUrl(),
        p.getTitle(),
        p.getContent(),
        imgs,
        p.getCreatedAt().toEpochMilli(),
        new PostController.PostCounts(likes, favorites, comments),
        liked,
        faved);
  }

  private String principal(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) return null;
    String u = String.valueOf(authentication.getPrincipal());
    u = u == null ? null : u.trim().toLowerCase(Locale.ROOT);
    return (u == null || u.isBlank()) ? null : u;
  }

  public record UserResponse(Long id, String username, String avatarUrl, String coverUrl, String bio) {}

  public record ErrorResponse(String message) {}
}

