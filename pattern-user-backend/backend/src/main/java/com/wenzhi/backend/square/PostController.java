package com.wenzhi.backend.square;

import com.wenzhi.backend.user.UserRepository;
import com.wenzhi.backend.upload.QiniuUrlService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final PostLikeRepository likeRepository;
  private final PostFavoriteRepository favoriteRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final QiniuUrlService urlService;

  public PostController(
      PostRepository postRepository,
      PostImageRepository postImageRepository,
      PostLikeRepository likeRepository,
      PostFavoriteRepository favoriteRepository,
      CommentRepository commentRepository,
      UserRepository userRepository,
      QiniuUrlService urlService) {
    this.postRepository = postRepository;
    this.postImageRepository = postImageRepository;
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
    this.urlService = urlService;
  }

  @GetMapping
  public PostPageResponse list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      Authentication authentication) {
    int safeSize = Math.max(1, Math.min(50, size));
    int safePage = Math.max(0, page);

    var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    var p = postRepository.findAll(pageable);
    String username = principal(authentication);
    Long userId = username == null ? null : userRepository.findByUsername(username).map(u -> u.getId()).orElse(null);
    var items =
        p.getContent().stream()
            .map(pe -> toPostResponse(pe, userId))
            .toList();
    return new PostPageResponse(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(@PathVariable("id") Long id, Authentication authentication) {
    var post = postRepository.findById(id).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    String username = principal(authentication);
    Long userId = username == null ? null : userRepository.findByUsername(username).map(u -> u.getId()).orElse(null);
    return ResponseEntity.ok(toPostResponse(post, userId));
  }

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody CreatePostRequest req, Authentication authentication) {
    String username = principal(authentication);
    if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("用户不存在"));

    String title = req.title == null ? "" : req.title.trim();
    String content = req.content == null ? "" : req.content.trim();
    if (title.length() < 3) return ResponseEntity.badRequest().body(new ErrorResponse("标题至少 3 个字"));
    if (content.length() < 10) return ResponseEntity.badRequest().body(new ErrorResponse("内容至少 10 个字"));
    List<String> images = req.images == null ? List.of() : req.images.stream().filter(s -> s != null && !s.isBlank()).toList();
    if (images.size() > 9) return ResponseEntity.badRequest().body(new ErrorResponse("最多 9 张图片"));

    var entity = new PostEntity();
    entity.setAuthor(user);
    entity.setTitle(title);
    entity.setContent(content);
    entity.setCreatedAt(Instant.now());
    var saved = postRepository.save(entity);

    for (int i = 0; i < images.size(); i++) {
      var img = new PostImageEntity();
      img.setPost(saved);
      img.setUrl(images.get(i));
      img.setSortOrder(i);
      postImageRepository.save(img);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(toPostResponse(saved, user.getId()));
  }

  @PutMapping("/{id}")
  @Transactional
  public ResponseEntity<?> update(
      @PathVariable("id") Long id, @Valid @RequestBody UpdatePostRequest req, Authentication authentication) {
    String username = principal(authentication);
    if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("用户不存在"));

    var post = postRepository.findById(id).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    if (!post.getAuthor().getId().equals(user.getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("无权限"));
    }

    String title = req.title == null ? "" : req.title.trim();
    String content = req.content == null ? "" : req.content.trim();
    if (title.length() < 3) return ResponseEntity.badRequest().body(new ErrorResponse("标题至少 3 个字"));
    if (content.length() < 10) return ResponseEntity.badRequest().body(new ErrorResponse("内容至少 10 个字"));

    post.setTitle(title);
    post.setContent(content);
    postRepository.save(post);
    List<String> images =
        req.images == null ? List.of() : req.images.stream().filter(s -> s != null && !s.isBlank()).toList();
    if (images.size() > 9) return ResponseEntity.badRequest().body(new ErrorResponse("最多 9 张图片"));
    postImageRepository.deleteByPostId(id);
    for (int i = 0; i < images.size(); i++) {
      var img = new PostImageEntity();
      img.setPost(post);
      img.setUrl(images.get(i));
      img.setSortOrder(i);
      postImageRepository.save(img);
    }
    return ResponseEntity.ok(toPostResponse(post, user.getId()));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<?> delete(@PathVariable("id") Long id, Authentication authentication) {
    String username = principal(authentication);
    if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("请先登录"));
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("用户不存在"));

    var post = postRepository.findById(id).orElse(null);
    if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    if (!post.getAuthor().getId().equals(user.getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("无权限"));
    }

    // clean up relations first to satisfy FK constraints
    likeRepository.deleteByIdPostId(id);
    favoriteRepository.deleteByIdPostId(id);
    commentRepository.deleteByPostId(id);
    postImageRepository.deleteByPostId(id);
    postRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private PostResponse toPostResponse(PostEntity p, Long viewerUserId) {
    Long postId = p.getId();
    var imgs =
        postImageRepository.findByPostIdOrderBySortOrderAscIdAsc(postId).stream()
            .map(PostImageEntity::getUrl)
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
    return new PostResponse(
        postId,
        author.getId(),
        author.getUsername(),
        author.getAvatarUrl(),
        p.getTitle(),
        p.getContent(),
        imgs,
        p.getCreatedAt().toEpochMilli(),
        new PostCounts(likes, favorites, comments),
        liked,
        faved);
  }

  private String principal(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) return null;
    String u = String.valueOf(authentication.getPrincipal());
    u = u == null ? null : u.trim().toLowerCase(Locale.ROOT);
    return (u == null || u.isBlank()) ? null : u;
  }

  public record CreatePostRequest(String title, String content, List<String> images) {}

  public record UpdatePostRequest(String title, String content, List<String> images) {}

  public record PostCounts(long likes, long favorites, long comments) {}

  public record PostResponse(
      Long id,
      Long authorId,
      String author,
      String avatarUrl,
      String title,
      String content,
      List<String> images,
      long createdAt,
      PostCounts counts,
      boolean liked,
      boolean faved) {}

  public record PostPageResponse(List<PostResponse> items, int page, int size, long total, int totalPages) {}

  public record ErrorResponse(String message) {}
}

