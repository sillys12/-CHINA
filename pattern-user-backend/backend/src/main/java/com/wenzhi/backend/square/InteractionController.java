package com.wenzhi.backend.square;

import com.wenzhi.backend.user.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InteractionController {
  private final PostRepository postRepository;
  private final PostLikeRepository likeRepository;
  private final PostFavoriteRepository favoriteRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  public InteractionController(
      PostRepository postRepository,
      PostLikeRepository likeRepository,
      PostFavoriteRepository favoriteRepository,
      CommentRepository commentRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  @PostMapping("/posts/{id}/like")
  public ResponseEntity<?> like(@PathVariable("id") Long postId, Authentication authentication) {
    var ctx = ctx(authentication);
    if (ctx == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var post = postRepository.findById(postId).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    var key = new UserPostKey(ctx.userId, postId);
    if (!likeRepository.existsById(key)) {
      var e = new PostLikeEntity();
      e.setUser(ctx.user);
      e.setPost(post);
      e.setId(key);
      likeRepository.save(e);
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/posts/{id}/like")
  public ResponseEntity<?> unlike(@PathVariable("id") Long postId, Authentication authentication) {
    var ctx = ctx(authentication);
    if (ctx == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    likeRepository.deleteById(new UserPostKey(ctx.userId, postId));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/posts/{id}/favorite")
  public ResponseEntity<?> favorite(@PathVariable("id") Long postId, Authentication authentication) {
    var ctx = ctx(authentication);
    if (ctx == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var post = postRepository.findById(postId).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    var key = new UserPostKey(ctx.userId, postId);
    if (!favoriteRepository.existsById(key)) {
      var e = new PostFavoriteEntity();
      e.setUser(ctx.user);
      e.setPost(post);
      e.setId(key);
      favoriteRepository.save(e);
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/posts/{id}/favorite")
  public ResponseEntity<?> unfavorite(@PathVariable("id") Long postId, Authentication authentication) {
    var ctx = ctx(authentication);
    if (ctx == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    favoriteRepository.deleteById(new UserPostKey(ctx.userId, postId));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/posts/{id}/comments")
  public ResponseEntity<?> addComment(
      @PathVariable("id") Long postId, @RequestBody CreateCommentRequest req, Authentication authentication) {
    var ctx = ctx(authentication);
    if (ctx == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var post = postRepository.findById(postId).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    String content = req == null || req.content == null ? "" : req.content.trim();
    if (content.isBlank()) return ResponseEntity.badRequest().body(new ErrorResponse("评论不能为空"));
    if (content.length() > 1000) return ResponseEntity.badRequest().body(new ErrorResponse("评论最多 1000 字"));
    var c = new CommentEntity();
    c.setPost(post);
    c.setAuthor(ctx.user);
    c.setContent(content);
    commentRepository.save(c);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  private Ctx ctx(Authentication authentication) {
    String username = principal(authentication);
    if (username == null) return null;
    var u = userRepository.findByUsername(username).orElse(null);
    if (u == null) return null;
    return new Ctx(u.getId(), u);
  }

  private String principal(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) return null;
    String u = String.valueOf(authentication.getPrincipal());
    u = u == null ? null : u.trim().toLowerCase(Locale.ROOT);
    return (u == null || u.isBlank()) ? null : u;
  }

  private record Ctx(Long userId, com.wenzhi.backend.user.UserEntity user) {}

  public record CreateCommentRequest(String content) {}

  public record ErrorResponse(String message) {}
}

