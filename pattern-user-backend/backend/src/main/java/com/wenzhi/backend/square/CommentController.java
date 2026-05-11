package com.wenzhi.backend.square;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  public CommentController(PostRepository postRepository, CommentRepository commentRepository) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
  }

  @GetMapping
  public ResponseEntity<?> list(@PathVariable("postId") Long postId) {
    if (!postRepository.existsById(postId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("帖子不存在"));
    }
    List<CommentResponse> out =
        commentRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId).stream()
            .map(
                c ->
                    new CommentResponse(
                        c.getId(),
                        c.getAuthor().getId(),
                        c.getAuthor().getUsername(),
                        c.getContent(),
                        c.getCreatedAt().toEpochMilli()))
            .toList();
    return ResponseEntity.ok(out);
  }

  public record CommentResponse(Long id, Long authorId, String author, String content, long createdAt) {}

  public record ErrorResponse(String message) {}
}

