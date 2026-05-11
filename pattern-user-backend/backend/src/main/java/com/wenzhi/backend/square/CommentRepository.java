package com.wenzhi.backend.square;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
  long countByPostId(Long postId);
  List<CommentEntity> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);
  void deleteByPostId(Long postId);
}

